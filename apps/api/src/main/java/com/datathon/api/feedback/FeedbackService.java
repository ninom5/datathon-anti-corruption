package com.datathon.api.feedback;

import com.datathon.api.common.InvalidRequestException;
import com.datathon.api.common.NotFoundException;
import com.datathon.api.common.persistence.DatabaseCode;
import com.datathon.api.contract.ContractService;
import com.datathon.api.feedback.dto.FeedbackRequest;
import com.datathon.api.feedback.dto.FeedbackResponse;
import com.datathon.api.indicator.IndicatorService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackService {

    /** The moderator has to answer these, so they need a contact address. */
    private static final Set<FeedbackKind> KINDS_NEEDING_CONTACT = Set.of(FeedbackKind.RIGHT_OF_REPLY,
            FeedbackKind.PERSONAL_DATA_REMOVAL);

    private final FeedbackRepository feedbackRepository;
    private final FeedbackRateLimiter rateLimiter;
    private final IndicatorService indicatorService;
    private final ContractService contractService;

    @Transactional
    public FeedbackResponse submit(FeedbackRequest request, String clientAddress) {
        rateLimiter.acquire(clientAddress);
        FeedbackTargetType targetType = codeOf(FeedbackTargetType.class, request.targetType());
        FeedbackKind kind = codeOf(FeedbackKind.class, request.kind());
        String contactEmail = blankToNull(request.contactEmail());
        if (KINDS_NEEDING_CONTACT.contains(kind) && contactEmail == null) {
            throw new InvalidRequestException("contactEmail: obavezno za pravo na odgovor i zahtjev za uklanjanje");
        }
        requireTargetExists(targetType, request.targetId());
        FeedbackRole role = request.role() == null ? null : codeOf(FeedbackRole.class, request.role());
        Feedback saved = feedbackRepository.save(new Feedback(targetType, request.targetId(), kind, role,
                request.text().strip(), contactEmail));
        log.info("Feedback {} received: {} on {} {}", saved.getId(), kind.code(), targetType.code(), request.targetId());
        return FeedbackResponse.from(saved);
    }

    /** Same visibility as the public pages: an inactive indicator or a hidden contract gets the same 404. */
    private void requireTargetExists(FeedbackTargetType targetType, Long targetId) {
        boolean exists = switch (targetType) {
            case INDICATOR -> indicatorService.isActive(targetId);
            case CONTRACT -> contractService.isPublic(targetId);
            case AI_ASSESSMENT, ORGANIZATION -> false;
        };
        if (!exists) {
            throw new NotFoundException("Zapis na koji se ispravak odnosi ne postoji.");
        }
    }

    private static <E extends Enum<E> & DatabaseCode> E codeOf(Class<E> type, String code) {
        return DatabaseCode.fromCode(type, code)
                .orElseThrow(() -> new InvalidRequestException(type.getSimpleName() + ": neispravna vrijednost"));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
