package com.datathon.api.assessment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import com.datathon.api.contract.Contract;
import com.datathon.api.organization.Organization;
import com.datathon.api.organization.OrganizationType;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/** Crafts and natural persons never get an AI assessment shown, whatever the table holds (GDPR, AGENTS.md). */
class AiAssessmentQueryServiceTest {

    @Test
    void craftGetsReasonInsteadOfAssessment() {
        AiAssessmentRepository repository = mock(AiAssessmentRepository.class);
        Contract contract = new Contract("k");
        contract.setSupplier(new Organization("00000000099", "Obrt Primjer", "obrt primjer", OrganizationType.CRAFT));

        AiAssessmentQueryService.ContractAssessment result =
                new AiAssessmentQueryService(repository, JsonMapper.shared()).forContract(contract);

        assertThat(result.assessment()).isNull();
        assertThat(result.unavailableReason()).isEqualTo(AiAssessmentQueryService.NOT_FOR_PERSONS);
        verifyNoInteractions(repository);
    }
}
