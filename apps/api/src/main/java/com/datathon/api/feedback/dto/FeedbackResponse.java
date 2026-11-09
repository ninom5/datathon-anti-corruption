package com.datathon.api.feedback.dto;

import com.datathon.api.feedback.Feedback;
import java.time.Instant;

public record FeedbackResponse(Long id, String targetType, Long targetId, String kind, String moderation, Instant createdAt) {

    public static FeedbackResponse from(Feedback feedback) {
        return new FeedbackResponse(feedback.getId(), feedback.getTargetType().code(), feedback.getTargetId(),
                feedback.getKind().code(), feedback.getModeration().code(), feedback.getCreatedAt());
    }
}
