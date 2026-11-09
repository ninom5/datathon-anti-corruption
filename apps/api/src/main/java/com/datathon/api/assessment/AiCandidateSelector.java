package com.datathon.api.assessment;

import com.datathon.api.wording.GenericSubjects;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AiCandidateSelector {

    private final AiAssessmentRepository assessmentRepository;
    private final AiProperties properties;

    public record Candidate(Long contractId, boolean hasIndicator, BigDecimal value, boolean vagueSubject) {
    }

    @Transactional(readOnly = true)
    public List<Candidate> select(boolean synthetic, Integer limit) {
        AiProperties.Selection selection = properties.selection();
        int max = limit == null ? selection.maxContractsPerRun() : Math.min(limit, selection.maxContractsPerRun());
        return allEligible(synthetic).stream().limit(Math.max(0, max)).toList();
    }

    @Transactional(readOnly = true)
    public List<Candidate> allEligible(boolean synthetic) {
        AiProperties.Selection selection = properties.selection();
        return assessmentRepository.findCandidateRows(synthetic).stream()
                .map(row -> toCandidate(row, selection))
                .filter(candidate -> isChosen(candidate, selection))
                .sorted(Comparator.comparing(Candidate::hasIndicator).reversed()
                        .thenComparing(Candidate::value, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Candidate::contractId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Candidate> selectByIds(List<Long> contractIds) {
        AiProperties.Selection selection = properties.selection();
        java.util.Map<Long, Candidate> eligible = new java.util.HashMap<>();
        for (Object[] row : assessmentRepository.findCandidateRows(false)) {
            Candidate candidate = toCandidate(row, selection);
            eligible.put(candidate.contractId(), candidate);
        }
        return contractIds.stream().distinct().map(eligible::get).filter(java.util.Objects::nonNull).toList();
    }

    private static Candidate toCandidate(Object[] row, AiProperties.Selection selection) {
        return new Candidate(((Number) row[0]).longValue(), (Boolean) row[3], (BigDecimal) row[2],
                GenericSubjects.isVague((String) row[1], selection.minSubjectWords()));
    }

    private static boolean isChosen(Candidate candidate, AiProperties.Selection selection) {
        boolean isLarge = candidate.value() != null && candidate.value().compareTo(selection.minValueEur()) >= 0;
        return candidate.hasIndicator() || isLarge || candidate.vagueSubject();
    }
}
