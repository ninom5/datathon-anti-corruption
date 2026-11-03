package com.datathon.api.assessment;

import com.datathon.api.common.persistence.DatabaseCode;
import com.datathon.api.contract.Contract;
import com.datathon.api.indicator.Indicator;
import com.datathon.api.indicator.dto.RuleTitles;
import com.datathon.api.organization.Organization;
import com.datathon.api.rules.ConcentrationRule;
import com.datathon.api.source.Provenance;
import com.datathon.api.source.RawRecord;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Builds the context of docs/ANOMALY_DETECTION.md §4.2 from database rows only. Keys are sorted (TreeMap), so the same
 * data always gives the same JSON and the same hash. No database access here: {@link AiContextLoader} loads the rows.
 */
final class AiContextBuilder {

    /** Sudreg has no key yet (docs/DATA_SOURCES.md), so NKD and founding date are unknown, never guessed. */
    static final String COMPANY_REGISTER_UNAVAILABLE = "nedostupno";

    private static final JsonMapper JSON = JsonMapper.shared();

    private AiContextBuilder() {
    }

    /**
     * Facts about a contract that come from other tables or from configuration.
     *
     * @param ownedByBuyer       true when JPT lists the buyer as a founder of the supplier; null when unknown
     * @param municipality       DZS 2021 population and peer group of the buyer; null when the buyer is not a JLS
     * @param peerStatistics     statistika_skupine, null when the peer comparison is not sufficient or not possible
     * @param simpleProcurement  ZJN simple procurement threshold valid on the signing date for the subject type
     *                           (thresholds.yaml), null when date or subject type is unknown
     */
    record ContextFacts(Boolean ownedByBuyer, MunicipalityFacts municipality, Map<String, Object> peerStatistics,
                        Threshold simpleProcurement) {
    }

    /** A value from thresholds.yaml with the legal source it was copied from. */
    record Threshold(BigDecimal valueEur, String source) {
    }

    static AiContext build(Contract contract, List<Indicator> indicators, ContextFacts facts) {
        Map<String, Object> peerStatistics = facts.peerStatistics();
        Map<String, Object> data = new TreeMap<>();
        data.put("ugovor", contractPart(contract, facts.simpleProcurement()));
        data.put("dobavljac", supplierPart(contract.getSupplier(), facts.ownedByBuyer()));
        data.put("narucitelj", buyerPart(contract.getBuyer(), facts.municipality()));
        data.put("pravila", indicators.stream()
                .filter(indicator -> isAboutThisSupplier(indicator, contract.getSupplier()))
                .map(AiContextBuilder::rulePart).toList());
        data.put("statistika_skupine", peerStatistics == null ? null : new TreeMap<>(peerStatistics));
        data.put("slobodni_tekst", freeTextPart(contract));
        Organization supplier = contract.getSupplier();
        return AiContext.of(contract.getId(), data, supplier == null ? null : supplier.getOrgType(),
                supplier == null ? null : supplier.getName());
    }

    private static Map<String, Object> contractPart(Contract contract, Threshold simpleProcurement) {
        Map<String, Object> part = new TreeMap<>();
        part.put("id", contract.getId());
        part.put("predmet", contract.getSubject());
        part.put("vrsta_predmeta", code(contract.getSubjectType()));
        part.put("cpv", contract.getCpv() == null ? null
                : contract.getCpv().strip() + (contract.getCpvLabel() == null ? "" : " - " + contract.getCpvLabel()));
        part.put("vrijednost_eur", value(contract));
        // The paid amount in registers usually includes VAT, so the model must see the gross value too (rule D2
        // compares with it); without it every +25 % payment looked like an overpayment (work/decisions.md, #44).
        part.put("vrijednost_s_pdv_eur", grossValue(contract));
        part.put("pocetna_vrijednost_eur", contract.getInitialValue());
        part.put("izmjene_eur", contract.getAmendmentsValue());
        part.put("isplaceno_eur", contract.getPaidAmount());
        part.put("datum", contract.getDateSigned() == null ? null : contract.getDateSigned().toString());
        part.put("vrsta_postupka", contract.getProcedureType());
        part.put("okvirni_sporazum", contract.isFrameworkBased());
        part.put("izvor_url", sourceUrl(contract.getProvenance()));
        part.put("eojn_url", contract.getEojnContractUrl());
        part.put("prag_jednostavne_nabave_eur", simpleProcurement == null ? null : simpleProcurement.valueEur());
        part.put("prag_izvor", simpleProcurement == null ? null : simpleProcurement.source());
        return part;
    }

    private static Map<String, Object> supplierPart(Organization supplier, Boolean ownedByBuyer) {
        Map<String, Object> part = new TreeMap<>();
        part.put("oib", supplier.getOib());
        part.put("naziv", supplier.getName());
        part.put("pravni_oblik", code(supplier.getOrgType()));
        part.put("nkd", null);
        part.put("datum_osnivanja", null);
        part.put("izvor", COMPANY_REGISTER_UNAVAILABLE);
        part.put("jpt_oznaka", supplier.getJptCode());
        part.put("u_vlasnistvu_narucitelja", ownedByBuyer);
        return part;
    }

    /** A JLS with its population (DZS, Popis 2021) and peer group P1-P7. */
    record MunicipalityFacts(Integer population, String peerGroup) {
    }

    private static Map<String, Object> buyerPart(Organization buyer, MunicipalityFacts municipality) {
        Map<String, Object> part = new TreeMap<>();
        part.put("oib", buyer.getOib());
        part.put("naziv", buyer.getName());
        part.put("vrsta", code(buyer.getOrgType()));
        part.put("stanovnika", municipality == null ? null : municipality.population());
        part.put("usporedna_skupina", municipality == null ? null : municipality.peerGroup());
        return part;
    }

    /**
     * Rule C is about a buyer and CPV division and is linked to every contract of that cell (#41), also to contracts of
     * other suppliers. For the model it is evidence only on contracts of the supplier it names (topSupplierOib);
     * on the others it would read as a statement about this supplier.
     */
    static boolean isAboutThisSupplier(Indicator indicator, Organization supplier) {
        if (!ConcentrationRule.RULE_CODE.equals(indicator.getRuleCode()) || indicator.getSubjectRef() == null) {
            return true;
        }
        String topSupplierOib = JSON.readTree(indicator.getSubjectRef()).path("topSupplierOib").asString(null);
        return topSupplierOib == null || topSupplierOib.equals(supplier.getOib());
    }

    private static Map<String, Object> rulePart(Indicator indicator) {
        Map<String, Object> part = new TreeMap<>();
        part.put("rule_code", indicator.getRuleCode());
        part.put("naziv", RuleTitles.of(indicator.getRuleCode()));
        part.put("magnitude", indicator.getMagnitudeLabel());
        part.put("pojas", code(indicator.getMagnitudeBand()));
        part.put("evidence_strength", code(indicator.getEvidenceStrength()));
        part.put("data_quality", code(indicator.getDataQuality()));
        part.put("oznaka", code(indicator.getSeverityNote()));
        part.put("objasnjenje", indicator.getExplanationHr());
        part.put("izvori", evidenceUrls(indicator.getEvidence()));
        return part;
    }

    private static Map<String, Object> freeTextPart(Contract contract) {
        Map<String, Object> part = new TreeMap<>();
        part.put("obrazlozenje_placanja", contract.getPaymentNote());
        part.put("napomena", contract.getNote());
        return part;
    }

    /** The exact row: file URL plus our locator ({@code sheet=1;row=41}) as fragment, as in §4.2. */
    static String sourceUrl(Provenance provenance) {
        RawRecord raw = provenance == null ? null : provenance.getRawRecord();
        if (raw == null || raw.getFile() == null || raw.getFile().getUrl() == null) {
            return null;
        }
        return raw.getLocator() == null ? raw.getFile().getUrl() : raw.getFile().getUrl() + "#" + raw.getLocator();
    }

    private static List<String> evidenceUrls(String evidenceJson) {
        List<String> urls = new ArrayList<>();
        if (evidenceJson == null) {
            return urls;
        }
        for (JsonNode link : JSON.readTree(evidenceJson)) {
            String url = link.path("url").asString(null);
            if (url != null && !url.isBlank() && !urls.contains(url)) {
                urls.add(url);
            }
        }
        return urls;
    }

    /** The value rules use: total with amendments, or the initial value when no total is published. */
    private static BigDecimal value(Contract contract) {
        return contract.getTotalValue() != null ? contract.getTotalValue() : contract.getInitialValue();
    }

    private static BigDecimal grossValue(Contract contract) {
        return contract.getTotalValueGross() != null ? contract.getTotalValueGross() : contract.getInitialValueGross();
    }

    private static String code(DatabaseCode value) {
        return value == null ? null : value.code();
    }
}
