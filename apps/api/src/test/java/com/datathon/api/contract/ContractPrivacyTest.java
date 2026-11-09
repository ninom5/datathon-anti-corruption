package com.datathon.api.contract;

import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.TestcontainersConfiguration;
import com.datathon.api.organization.Organization;
import com.datathon.api.organization.OrganizationRepository;
import com.datathon.api.organization.OrganizationType;
import com.datathon.api.rules.RulesService;
import com.datathon.api.source.ExtractionMethod;
import com.datathon.api.source.Provenance;
import com.datathon.api.source.ProvenanceRepository;
import com.datathon.api.source.RawRecord;
import com.datathon.api.source.RawRecordRepository;
import com.datathon.api.source.SourceDataset;
import com.datathon.api.source.SourceDatasetRepository;
import com.datathon.api.source.SourceFile;
import com.datathon.api.source.SourceFileRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ContractPrivacyTest {

    private static final String CRAFT_NAME = "Obrt Primjer, vl. Pero Primjerić";
    private static final String CRAFT_OIB = "99999999901";
    private static final String PERSON_OIB = "99999999902";
    private static final String BUYER_OIB = "99999999903";

    @Autowired
    private MockMvcTester mvc;
    @Autowired
    private SourceDatasetRepository datasetRepository;
    @Autowired
    private SourceFileRepository fileRepository;
    @Autowired
    private RawRecordRepository rawRecordRepository;
    @Autowired
    private ProvenanceRepository provenanceRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private RulesService rulesService;

    @Test
    void craftNameIsHiddenNaturalPersonContractIsNotExposedAndCraftsStayOutOfNamedLists() throws Exception {
        SourceFile file = sourceFile();
        Organization buyer = organization(BUYER_OIB, "Općina Testno", OrganizationType.LOCAL_GOVERNMENT);
        Contract craftContract = contract("craft", buyer, organization(CRAFT_OIB, CRAFT_NAME, OrganizationType.CRAFT), file, 2);
        Contract personContract = contract("person", buyer, organization(PERSON_OIB, "Pero Primjerić", OrganizationType.NATURAL_PERSON), file, 3);
        rulesService.runRules();

        MvcTestResult detail = mvc.get().uri("/api/v1/contracts/" + craftContract.getId()).exchange();
        assertThat(detail).hasStatusOk();
        String body = detail.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertThat(body).doesNotContain("Pero").doesNotContain("Primjerić");
        assertThat(detail).bodyJson().satisfies(json -> {
            assertThat(json).extractingPath("$.supplier.name").isEqualTo("Obrt (naziv skriven)");
            assertThat(json).extractingPath("$.supplier.nameHidden").isEqualTo(true);
            assertThat(json).extractingPath("$.supplier.oib").isEqualTo(CRAFT_OIB);
            assertThat(json).extractingPath("$.provenance.fileUrl").isEqualTo(file.getUrl());
            assertThat(json).extractingPath("$.indicators[0].ruleCode").isEqualTo("D_VALUE_INCREASE");
        });

        assertThat(mvc.get().uri("/api/v1/contracts?buyerOib=" + BUYER_OIB)).bodyJson().satisfies(json -> {
            assertThat(json).extractingPath("$.totalElements").isEqualTo(1);
            assertThat(json).extractingPath("$.content[0].supplier.name").isEqualTo("Obrt (naziv skriven)");
        });
        assertThat(mvc.get().uri("/api/v1/contracts?supplierOib=" + PERSON_OIB))
                .bodyJson().extractingPath("$.totalElements").isEqualTo(0);
        assertThat(mvc.get().uri("/api/v1/contracts/" + personContract.getId())).hasStatus(404);

        assertThat(mvc.get().uri("/api/v1/contracts/summary?buyerOib=" + BUYER_OIB)).bodyJson().satisfies(json -> {
            assertThat(json).extractingPath("$.contracts").isEqualTo(1);
            assertThat(json).extractingPath("$.withIndicator").isEqualTo(1);
            assertThat(json).extractingPath("$.craftContracts").isEqualTo(1);
            assertThat(json).extractingPath("$.contractsByRule.D_VALUE_INCREASE").isEqualTo(1);
        });

        MvcTestResult profile = mvc.get().uri("/api/v1/buyers/" + BUYER_OIB).exchange();
        assertThat(profile).hasStatusOk();
        assertThat(profile.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8))
                .doesNotContain(CRAFT_NAME).doesNotContain("Pero");
        assertThat(profile).bodyJson().satisfies(json -> {
            assertThat(json).extractingPath("$.contracts").isEqualTo(1);
            assertThat(json).extractingPath("$.craftContracts").isEqualTo(1);
            assertThat(json).extractingPath("$.topSuppliers").asArray().isEmpty();
            assertThat(json).extractingPath("$.indicatorCounts[0].ruleCode").isEqualTo("D_VALUE_INCREASE");
        });
    }

    private SourceFile sourceFile() {
        SourceDataset dataset = new SourceDataset(SourceDataset.PORTAL_DATA_GOV_HR, "privacy-test");
        dataset.setTitle("Registar ugovora Općine Testno");
        dataset.setPublisher("Općina Testno");
        datasetRepository.save(dataset);
        return fileRepository.save(new SourceFile(dataset, "https://testno.invalid/registar.xlsx", Instant.now()));
    }

    private Organization organization(String oib, String name, OrganizationType type) {
        return organizationRepository.save(new Organization(oib, name, name.toLowerCase(), type));
    }

    private Contract contract(String key, Organization buyer, Organization supplier, SourceFile file, int row) {
        RawRecord raw = rawRecordRepository.save(new RawRecord(file, "sheet=1;row=" + row,
                "{\"Naziv ugovaratelja\": \"" + supplier.getName() + "\"}"));
        Provenance provenance = provenanceRepository.save(new Provenance(raw, ExtractionMethod.PARSER, "test", BigDecimal.ONE));
        Contract contract = new Contract("https://testno.invalid/contract-eo/" + key);
        contract.setBuyer(buyer);
        contract.setSupplier(supplier);
        contract.setSubject("Uređenje nerazvrstane ceste");
        contract.setSubjectType(SubjectType.WORKS);
        contract.setDateSigned(LocalDate.of(2025, 3, 1));
        contract.setCurrencyOriginal("EUR");
        contract.setInitialValue(new BigDecimal("100000"));
        contract.setAmendmentsValue(new BigDecimal("40000"));
        contract.setTotalValue(new BigDecimal("140000"));
        contract.setProvenance(provenance);
        contract.setValidFrom(Instant.now());
        return contractRepository.save(contract);
    }
}
