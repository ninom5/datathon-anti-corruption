package com.datathon.api.assessment;

import com.datathon.api.assessment.dto.AiAssessmentResponse;
import com.datathon.api.contract.Contract;
import com.datathon.api.organization.OrganizationType;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
public class AiAssessmentQueryService {

    public static final String NOT_FOR_PERSONS = "AI procjena se ne radi za obrte i fizičke osobe.";
    public static final String NOT_AVAILABLE = "AI procjena nije dostupna.";

    private final AiAssessmentRepository assessmentRepository;
    private final JsonMapper jsonMapper;

    public record ContractAssessment(AiAssessmentResponse assessment, String unavailableReason) {
    }

    @Transactional(readOnly = true)
    public ContractAssessment forContract(Contract contract) {
        if (isPerson(contract)) {
            return new ContractAssessment(null, NOT_FOR_PERSONS);
        }
        Optional<AiAssessment> latest = assessmentRepository.findLatest(contract.getId())
                .filter(assessment -> assessment.getStatus() == AiAssessmentStatus.ACCEPTED);
        return latest.map(assessment -> new ContractAssessment(toResponse(assessment), null))
                .orElseGet(() -> new ContractAssessment(null, NOT_AVAILABLE));
    }

    private static boolean isPerson(Contract contract) {
        OrganizationType type = contract.getSupplier() == null ? null : contract.getSupplier().getOrgType();
        return type == OrganizationType.CRAFT || type == OrganizationType.NATURAL_PERSON;
    }

    private AiAssessmentResponse toResponse(AiAssessment assessment) {
        List<AiAnswer.Reason> reasons = jsonMapper.readValue(assessment.getReasons(), new TypeReference<>() {
        });
        return new AiAssessmentResponse(assessment.getId(), assessment.getLabel().code(),
                assessment.getConfidence().code(),
                reasons.stream().map(reason -> new AiAssessmentResponse.Reason(reason.claim(), reason.fieldsOrEmpty(),
                        reason.sourcesOrEmpty())).toList(),
                strings(assessment.getMissingData()), strings(assessment.getLegitExplanations()),
                assessment.getModel(), assessment.getPromptVersion(), assessment.getCreatedAt(),
                AiAssessmentResponse.BADGE);
    }

    private List<String> strings(String json) {
        return jsonMapper.readValue(json, new TypeReference<>() {
        });
    }
}
