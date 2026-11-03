package com.datathon.api.indicator;

import com.datathon.api.contract.Contract;
import com.datathon.api.organization.Organization;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
    /** Rule F: contracts of one buyer, supplier and CPV group inside a time window. */
    public static final String SUBJECT_GROUP = "group";
    /** Rule C: one buyer and CPV division over a period. */
    public static final String SUBJECT_BUYER_CPV = "buyer_cpv";

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id")
    private Organization buyer;

    /** Every contract the indicator is about: one for contract rules, the whole group for F and C. */
    @ElementCollection
    @CollectionTable(name = "indicator_contract", joinColumns = @JoinColumn(name = "indicator_id"))
    @Column(name = "contract_id")
    private List<Long> contractIds = new ArrayList<>();

    private BigDecimal magnitudeValue;
    private String magnitudeUnit;
    private String magnitudeLabel;
    private MagnitudeBand magnitudeBand;
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
        this.buyer = contract.getBuyer();
        this.contractIds.add(contract.getId());
        this.computedAt = Instant.now();
    }

    /**
     * "Informativno" means a probable entry error only for rule D (implausible numbers in the register). For F and C it
     * means legitimate context (the buyer's own company, recurring supplies), which is not an error in the source.
     */
    public static boolean isPossibleEntryError(Indicator indicator) {
        return indicator.getSeverityNote() == SeverityNote.INFORMATIVE && indicator.getRuleCode().startsWith("D_");
    }

    /** An indicator about a group of contracts of one buyer (rules F and C). */
    public Indicator(String ruleCode, String ruleVersion, String subjectType, Organization buyer, List<Long> contractIds) {
        this.ruleCode = ruleCode;
        this.ruleVersion = ruleVersion;
        this.subjectType = subjectType;
        this.buyer = buyer;
        this.contractIds.addAll(contractIds);
        this.computedAt = Instant.now();
    }
}
