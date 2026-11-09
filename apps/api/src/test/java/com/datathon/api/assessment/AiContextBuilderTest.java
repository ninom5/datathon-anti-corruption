package com.datathon.api.assessment;

import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.assessment.AiContextBuilder.MunicipalityFacts;
import com.datathon.api.contract.Contract;
import com.datathon.api.indicator.Indicator;
import com.datathon.api.organization.Organization;
import com.datathon.api.organization.OrganizationType;
import com.datathon.api.source.ExtractionMethod;
import com.datathon.api.source.Provenance;
import com.datathon.api.source.RawRecord;
import com.datathon.api.source.SourceDataset;
import com.datathon.api.source.SourceFile;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AiContextBuilderTest {

    private static final String FILE_URL = "https://primjer.invalid/registar.xlsx";
    private static final String EVIDENCE_URL = "https://primjer.invalid/contract-eo/S001";

    private static Contract contract() {
        Organization buyer = new Organization("00000000001", "Općina Primjerovo", "opcina primjerovo",
                OrganizationType.LOCAL_GOVERNMENT);
        Organization supplier = new Organization("00000000012", "Uzorak gradnja d.o.o.", "uzorak gradnja",
                OrganizationType.LLC);
        SourceFile file = new SourceFile(new SourceDataset("synthetic", "primjer"), FILE_URL, Instant.now());
        RawRecord raw = new RawRecord(file, "sheet=1;row=41", "{}");
        Contract contract = new Contract("https://primjer.invalid/contract-eo/S001");
        ReflectionTestUtils.setField(contract, "id", 7L);
        contract.setBuyer(buyer);
        contract.setSupplier(supplier);
        contract.setSubject("Rekonstrukcija nerazvrstane ceste");
        contract.setCpv("45000000");
        contract.setCpvLabel("Građevinski radovi");
        contract.setInitialValue(new BigDecimal("200000.00"));
        contract.setTotalValue(new BigDecimal("350000.00"));
        contract.setDateSigned(LocalDate.of(2025, 3, 10));
        contract.setProcedureType("Otvoreni postupak");
        contract.setProvenance(new Provenance(raw, ExtractionMethod.PARSER, "test", BigDecimal.ONE));
        return contract;
    }

    private static Indicator indicator(Contract contract) {
        Indicator indicator = new Indicator("D_VALUE_INCREASE", "rules-v1", contract);
        indicator.setMagnitudeLabel("+75 %");
        indicator.setExplanationHr("Vrijednost je porasla za 75 %.");
        indicator.setEvidence("[{\"label\":\"EOJN\",\"url\":\"" + EVIDENCE_URL + "\",\"field\":\"total\"}]");
        return indicator;
    }

    private static AiContext build() {
        Contract contract = contract();
        return AiContextBuilder.build(contract, List.of(indicator(contract)), new AiContextBuilder.ContextFacts(null,
                new MunicipalityFacts(3120, "P2"), null, new AiContextBuilder.Threshold(new BigDecimal("26540"), "ZJN"), null));
    }

    @Test
    void sameDataGivesSameJsonAndHash() {
        AiContext first = build();
        AiContext second = build();

        assertThat(first.canonicalJson()).isEqualTo(second.canonicalJson());
        assertThat(ContextHash.of(AiPrompt.VERSION, "claude-haiku-5-5", first.canonicalJson()))
                .isEqualTo(ContextHash.of(AiPrompt.VERSION, "claude-haiku-5-5", second.canonicalJson()))
                .hasSize(64)
                .isNotEqualTo(ContextHash.of(AiPrompt.VERSION, "claude-sonnet-5-5", first.canonicalJson()));
    }

    @Test
    void keysAreSortedAndCompanyRegisterDataIsNeverInvented() {
        AiContext context = build();

        assertThat(context.canonicalJson()).startsWith("{\"dobavljac\":{\"datum_osnivanja\":null,\"djelatnost\":null,\"izvor\":\"nedostupno\"");
        @SuppressWarnings("unchecked")
        Map<String, Object> supplier = (Map<String, Object>) context.data().get("dobavljac");
        assertThat(supplier).containsEntry("nkd", null).containsEntry("datum_osnivanja", null)
                .containsEntry("izvor", AiContextBuilder.COMPANY_REGISTER_UNAVAILABLE);
        assertThat(context.data().get("statistika_skupine")).isNull();
    }

    @Test
    void companyRegisterDataComesFromTheSnapshotWithItsSource() {
        Contract contract = contract();
        AiContextBuilder.CompanyFacts company = new AiContextBuilder.CompanyFacts(true, LocalDate.of(2026, 11, 4),
                LocalDate.of(2020, 12, 9), "41.00.0", "Građenje stambenih i nestambenih zgrada", "NKD 2025", false, false);
        AiContextBuilder.CompanyFacts missing = new AiContextBuilder.CompanyFacts(false, LocalDate.of(2026, 11, 4),
                null, null, null, null, null, null);

        AiContext found = AiContextBuilder.build(contract, List.of(),
                new AiContextBuilder.ContextFacts(null, null, null, null, company));
        AiContext notFound = AiContextBuilder.build(contract, List.of(),
                new AiContextBuilder.ContextFacts(null, null, null, null, missing));

        @SuppressWarnings("unchecked")
        Map<String, Object> supplier = (Map<String, Object>) found.data().get("dobavljac");
        assertThat(supplier).containsEntry("nkd", "41.00.0").containsEntry("datum_osnivanja", "2020-12-09")
                .containsEntry("djelatnost", "Građenje stambenih i nestambenih zgrada");
        assertThat((String) supplier.get("izvor")).startsWith("Sudski registar").contains("NKD 2025");
        @SuppressWarnings("unchecked")
        Map<String, Object> unknown = (Map<String, Object>) notFound.data().get("dobavljac");
        assertThat(unknown).containsEntry("nkd", null).containsEntry("datum_osnivanja", null);
        assertThat((String) unknown.get("izvor")).startsWith("nije u Sudskom registru");
    }

    @Test
    void concentrationIndicatorOfAnotherSupplierIsLeftOut() {
        Contract contract = contract();
        Indicator otherSupplier = new Indicator("C_CONCENTRATION", "rules-v1", "buyer_cpv", contract.getBuyer(),
                List.of(7L));
        otherSupplier.setSubjectRef("{\"topSupplierOib\":\"00000000011\"}");
        Indicator thisSupplier = new Indicator("C_CONCENTRATION", "rules-v1", "buyer_cpv", contract.getBuyer(),
                List.of(7L));
        thisSupplier.setSubjectRef("{\"topSupplierOib\":\"00000000012\"}");

        AiContext context = AiContextBuilder.build(contract, List.of(otherSupplier, thisSupplier, indicator(contract)),
                new AiContextBuilder.ContextFacts(null, null, null, null, null));

        assertThat((List<?>) context.data().get("pravila")).hasSize(2);
        assertThat(context.canonicalJson()).doesNotContain("00000000011");
    }

    @Test
    void citablePathsAndUrlsComeOnlyFromTheContext() {
        AiContext context = build();

        assertThat(context.fieldPaths()).contains("ugovor.predmet", "ugovor.vrijednost_eur", "narucitelj.stanovnika",
                "ugovor.prag_jednostavne_nabave_eur",
                "pravila", "pravila[0].magnitude", "pravila.magnitude", "dobavljac.izvor");
        assertThat(context.fieldPaths()).contains("dobavljac.nkd", "statistika_skupine", "ugovor.isplaceno_eur")
                .doesNotContain("dobavljac.broj_zaposlenih", "ugovor.iznos");
        assertThat(context.sourceUrls()).containsExactlyInAnyOrder(FILE_URL + "#sheet=1;row=41", EVIDENCE_URL);
        assertThat(context.data()).containsOnlyKeys("ugovor", "dobavljac", "narucitelj", "pravila",
                "statistika_skupine", "slobodni_tekst");
    }
}
