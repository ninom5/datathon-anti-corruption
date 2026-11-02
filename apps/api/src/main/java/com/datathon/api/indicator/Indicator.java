package com.datathon.api.indicator;

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
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Result of one deterministic rule for one subject. The three dimensions (data quality, magnitude, evidence strength)
 * are separate fields on purpose: there is no combined score (docs/ANOMALY_DETECTION.md §0.2).
 */
@Entity
@Table(name = "indicator")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Indicator {

    public static final String SUBJECT_CONTRACT = "contract";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ruleCode;
    private String ruleVersion;
    private String subjectType;

    @JdbcTypeCode(SqlTypes.JSON)
    private String subjectRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contract contract;

    private BigDecimal magnitudeValue;
    private String magnitudeUnit;
    private String magnitudeLabel;
    private DataQuality dataQuality;
    private EvidenceStrength evidenceStrength;
    private SeverityNote severityNote;
    private String explanationHr;
    private String notMeaningHr;

    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<String> missingData = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    private String evidence;

    private IndicatorStatus status = IndicatorStatus.ACTIVE;
    private Instant computedAt;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(columnDefinition = "char(64)")
    private String inputHash;

    public Indicator(String ruleCode, String ruleVersion, Contract contract) {
        this.ruleCode = ruleCode;
        this.ruleVersion = ruleVersion;
        this.subjectType = SUBJECT_CONTRACT;
        this.contract = contract;
        this.computedAt = Instant.now();
    }
}
