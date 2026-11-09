package com.datathon.api.assessment;

import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.TestcontainersConfiguration;
import com.datathon.api.contract.Contract;
import com.datathon.api.contract.ContractRepository;
import com.datathon.api.ingest.synthetic.SyntheticRegisterLoader;
import com.datathon.api.integration.anthropic.FixtureLlmClient;
import com.datathon.api.integration.anthropic.LlmClient;
import com.datathon.api.organization.OrganizationType;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {"javniradar.ai.concurrency=1", "javniradar.ai.max-usd-per-run=0.0005"})
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, AiRunServiceTest.FixtureConfig.class})
class AiRunServiceTest {

    @TestConfiguration(proxyBeanMethods = false)
    static class FixtureConfig {
        @Bean
        @Primary
        FixtureLlmClient fixtureLlmClient() {
            return new FixtureLlmClient();
        }
    }

    @Autowired
    private SyntheticRegisterLoader syntheticLoader;
    @Autowired
    private AiRunService aiRunService;
    @Autowired
    private AiCandidateSelector candidateSelector;
    @Autowired
    private AiAssessmentRepository assessmentRepository;
    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private FixtureLlmClient fixtureClient;
    @Autowired
    private LlmClient llmClient;
    @Autowired
    private MockMvcTester mvc;

    @Test
    void runStoresAcceptedAndRejectedAnswersUsesTheCacheAndStopsAtTheBudget() {
        syntheticLoader.load();
        assertThat(llmClient).isSameAs(fixtureClient);
        List<AiCandidateSelector.Candidate> candidates = candidateSelector.select(true, null);
        assertThat(candidates).hasSizeGreaterThan(20);
        assertThat(candidates.getFirst().hasIndicator()).isTrue();
        assertNoCraftsOrPersons(candidates);

        fixtureClient.answerAlwaysWith("vrijedi-provjeriti");
        AiRunResult first = aiRunService.run(true, 2, false);
        assertThat(first.called()).isEqualTo(2);
        assertThat(first.accepted()).isEqualTo(2);
        assertThat(first.costUsd()).isEqualByComparingTo(new BigDecimal("0.0004"));
        assertThat(first.stopReason()).isNull();

        AiRunResult cached = aiRunService.run(true, 2, false);
        assertThat(cached.cacheHits()).isEqualTo(2);
        assertThat(cached.called()).isZero();
        assertThat(fixtureClient.calls()).isEqualTo(2);

        fixtureClient.answerAlwaysWith("izmisljeno-polje");
        AiRunResult budget = aiRunService.run(true, 6, false);
        assertThat(budget.cacheHits()).isEqualTo(2);
        assertThat(budget.called()).isEqualTo(3);
        assertThat(budget.rejected()).isEqualTo(3);
        assertThat(budget.notProcessed()).isEqualTo(1);
        assertThat(budget.stopReason()).contains("budžet");

        int callsBeforeDryRun = fixtureClient.calls();
        AiRunResult dryRun = aiRunService.run(true, 6, true);
        assertThat(dryRun.toCall()).isEqualTo(1);
        assertThat(dryRun.cacheHits()).isEqualTo(5);
        assertThat(dryRun.estimatedCostUsd()).isPositive();
        assertThat(dryRun.sampleContext()).containsKeys("ugovor", "dobavljac", "pravila");
        assertThat(fixtureClient.calls()).isEqualTo(callsBeforeDryRun);

        fixtureClient.answerAlwaysWith(FixtureLlmClient.UNAVAILABLE);
        AiRunResult apiDown = aiRunService.run(true, 20, false);
        assertThat(apiDown.errors()).isEqualTo(AiRunService.MAX_CONSECUTIVE_ERRORS);
        assertThat(apiDown.stopReason()).contains("zaredom");
        assertThat(apiDown.notProcessed()).isEqualTo(20 - 5 - AiRunService.MAX_CONSECUTIVE_ERRORS);

        assertDetailShowsOnlyAccepted(candidates.get(0).contractId(), candidates.get(2).contractId());
    }

    private void assertDetailShowsOnlyAccepted(Long acceptedId, Long rejectedId) {
        AiAssessment rejected = assessmentRepository.findLatest(rejectedId).orElseThrow();
        assertThat(rejected.getStatus()).isEqualTo(AiAssessmentStatus.REJECTED);
        assertThat(rejected.getLabel()).isEqualTo(AiLabel.NO_FLAG);
        assertThat(rejected.getRejectionReason()).contains("dobavljac.broj_zaposlenih");

        assertThat(mvc.get().uri("/api/v1/contracts/" + acceptedId))
                .hasStatusOk()
                .bodyJson()
                .satisfies(json -> {
                    assertThat(json).extractingPath("$.aiAssessment.badge")
                            .isEqualTo("AI procjena – zahtijeva ljudsku provjeru");
                    assertThat(json).extractingPath("$.aiAssessment.label").isEqualTo("vrijedi_provjeriti");
                    assertThat(json).extractingPath("$.aiAssessment.model").isEqualTo("claude-haiku-5-5");
                    assertThat(json).extractingPath("$.aiAssessment.promptVersion").isEqualTo(AiPrompt.VERSION);
                    assertThat(json).extractingPath("$.aiAssessment.reasons[0].sources[0]").asString()
                            .startsWith(SyntheticRegisterLoader.FILE_URL + "#");
                    assertThat(json).extractingPath("$.aiAssessmentUnavailableReason").isNull();
                });
        assertThat(mvc.get().uri("/api/v1/contracts/" + rejectedId))
                .hasStatusOk()
                .bodyJson()
                .satisfies(json -> {
                    assertThat(json).extractingPath("$.aiAssessment").isNull();
                    assertThat(json).extractingPath("$.aiAssessmentUnavailableReason")
                            .isEqualTo(AiAssessmentQueryService.NOT_AVAILABLE);
                });
    }

    @Transactional(readOnly = true)
    void assertNoCraftsOrPersons(List<AiCandidateSelector.Candidate> candidates) {
        List<Contract> contracts = contractRepository.findByIdInOrderByDateSignedAscIdAsc(
                candidates.stream().map(AiCandidateSelector.Candidate::contractId).toList());
        assertThat(contracts).allSatisfy(contract -> {
            assertThat(contract.getSupplier().getOib()).isNotNull();
            assertThat(contract.getSupplier().getOrgType())
                    .isNotIn(OrganizationType.CRAFT, OrganizationType.NATURAL_PERSON);
            assertThat(contract.isSynthetic()).isTrue();
        });
    }
}
