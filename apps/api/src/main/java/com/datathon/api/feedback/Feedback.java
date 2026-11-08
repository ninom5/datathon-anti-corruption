package com.datathon.api.feedback;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A correction, context or right of reply sent through the form (F12). Published only after moderation. */
@Entity
@Table(name = "feedback")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private FeedbackTargetType targetType;
    private Long targetId;
    private FeedbackKind kind;
    private FeedbackRole role;
    private String text;

    /** Never returned by the API or logged; only for the moderator's reply. */
    private String contactEmail;

    private ModerationStatus moderation = ModerationStatus.NEW;
    private Instant createdAt = Instant.now();

    public Feedback(FeedbackTargetType targetType, Long targetId, FeedbackKind kind, FeedbackRole role, String text,
                    String contactEmail) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.kind = kind;
        this.role = role;
        this.text = text;
        this.contactEmail = contactEmail;
    }
}
