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

final class AiContextBuilder {

    static final String COMPANY_REGISTER_UNAVAILABLE = "nedostupno";

    private static final JsonMapper JSON = JsonMapper.shared();

    private AiContextBuilder() {
    }

    record ContextFacts(Boolean ownedByBuyer, MunicipalityFacts municipality, Map<String, Object> peerStatistics,
                        Threshold simpleProcurement, CompanyFacts company) {
    }

    record CompanyFacts(boolean foundInRegister, java.time.LocalDate retrievedOn, java.time.LocalDate foundedOn,
                        String nkdCode, String nkdName, String nkdVersion, Boolean inBankruptcy,
                        Boolean inLiquidation) {
    }

    record Threshold(BigDecimal valueEur, String source) {
    }

    static AiContext build(Contract contract, List<Indicator> indicators, ContextFacts facts) {
        Map<String, Object> peerStatistics = facts.peerStatistics();
        Map<String, Object> data = new TreeMap<>();
        data.put("ugovor", contractPart(contract, facts.simpleProcurement()));
        data.put("dobavljac", supplierPart(contract.getSupplier(), facts.ownedByBuyer(), facts.company()));
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

    private static Map<String, Object> supplierPart(Organization supplier, Boolean ownedByBuyer, CompanyFacts company) {
        Map<String, Object> part = new TreeMap<>();
        part.put("oib", supplier.getOib());
        part.put("naziv", supplier.getName());
        part.put("pravni_oblik", code(supplier.getOrgType()));
        part.put("jpt_oznaka", supplier.getJptCode());
        part.put("u_vlasnistvu_narucitelja", ownedByBuyer);
        boolean isKnown = company != null && company.foundInRegister();
        part.put("nkd", isKnown ? company.nkdCode() : null);
        part.put("djelatnost", isKnown ? company.nkdName() : null);
        part.put("datum_osnivanja", isKnown && company.foundedOn() != null ? company.foundedOn().toString() : null);
        part.put("u_stecaju", isKnown ? company.inBankruptcy() : null);
        part.put("u_likvidaciji", isKnown ? company.inLiquidation() : null);
        part.put("izvor", registerSource(company));
        return part;
    }

    private static String registerSource(CompanyFacts company) {
        if (company == null) {
            return COMPANY_REGISTER_UNAVAILABLE;
        }
        if (!company.foundInRegister()) {
            return "nije u Sudskom registru (provjereno " + company.retrievedOn() + ")";
        }
        String version = company.nkdVersion() == null ? "djelatnost nije upisana" : company.nkdVersion();
        return "Sudski registar, sudreg-data.gov.hr (" + version + ", dohvaćeno " + company.retrievedOn() + ")";
    }

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
