package com.datathon.api.feedback.dto;

import com.datathon.api.feedback.Feedback;
import java.time.Instant;

/** What the sender gets back: an id to quote and the moderation state. The e-mail is never echoed. */
public record FeedbackResponse(Long id, String targetType, Long targetId, String kind, String moderation, Instant createdAt) {

    public static FeedbackResponse from(Feedback feedback) {
        return new FeedbackResponse(feedback.getId(), feedback.getTargetType().code(), feedback.getTargetId(),
                feedback.getKind().code(), feedback.getModeration().code(), feedback.getCreatedAt());
    }
}
