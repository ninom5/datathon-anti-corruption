package com.datathon.api.assessment;

import com.datathon.api.contract.Contract;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "ai_assessment")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contract contract;

    private AiLabel label;

    @JdbcTypeCode(SqlTypes.JSON)
    private String reasons;

    @JdbcTypeCode(SqlTypes.JSON)
    private String missingData;

    @JdbcTypeCode(SqlTypes.JSON)
    private String legitExplanations;

    private AiConfidence confidence;
    private String model;
    private String promptVersion;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(columnDefinition = "char(64)")
    private String inputHash;

    private AiAssessmentStatus status;
    private String rejectionReason;
    private Instant createdAt;

    public AiAssessment(Contract contract, String model, String promptVersion, String inputHash) {
        this.contract = contract;
        this.model = model;
        this.promptVersion = promptVersion;
        this.inputHash = inputHash;
        this.createdAt = Instant.now();
    }
}
