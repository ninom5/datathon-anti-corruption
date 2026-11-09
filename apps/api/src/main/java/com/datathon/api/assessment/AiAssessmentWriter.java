package com.datathon.api.assessment;

import com.datathon.api.contract.ContractRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class AiAssessmentWriter {

    private final AiAssessmentRepository assessmentRepository;
    private final LlmUsageRepository usageRepository;
    private final ContractRepository contractRepository;
    private final JsonMapper jsonMapper;

    @Transactional
    public AiAssessment save(Long contractId, String model, String inputHash, AiValidation validation) {
        AiAssessment assessment = new AiAssessment(contractRepository.getReferenceById(contractId), model,
                AiPrompt.VERSION, inputHash);
        assessment.setLabel(validation.label());
        assessment.setConfidence(validation.confidence());
        assessment.setReasons(jsonMapper.writeValueAsString(validation.reasons()));
        assessment.setMissingData(jsonMapper.writeValueAsString(validation.missingData()));
        assessment.setLegitExplanations(jsonMapper.writeValueAsString(validation.legitExplanations()));
        assessment.setStatus(validation.status());
        assessment.setRejectionReason(validation.rejectionReason());
        return assessmentRepository.save(assessment);
    }

    @Transactional
    public void recordUsage(String purpose, String model, long inputTokens, long outputTokens, BigDecimal costUsd,
                            boolean cacheHit) {
        usageRepository.save(new LlmUsage(purpose, model, Math.toIntExact(inputTokens),
                Math.toIntExact(outputTokens), costUsd, cacheHit));
    }
}
