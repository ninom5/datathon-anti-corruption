package com.datathon.api.rules;

import com.datathon.api.contract.SubjectType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Thresholds from {@code config/thresholds.yaml}. Every legal value has a validity period and a source; a period is
 * chosen by the contract's signing date, never by today's date (docs/DATA_SOURCES.md §10).
 */
@ConfigurationProperties("javniradar.thresholds")
public record RuleThresholds(List<ModificationLimits> contractModification, List<SimpleProcurement> simpleProcurement,
                             RuleD ruleD) {

    /** ZJN limits for contract modifications, as a share of the initial value. */
    public record ModificationLimits(LocalDate validFrom, LocalDate validTo, BigDecimal deMinimisGoodsServices,
                                     BigDecimal deMinimisWorks, BigDecimal additionalSupplies,
                                     BigDecimal unforeseenCircumstances, String source) implements Period {

        public BigDecimal deMinimis(SubjectType subjectType) {
            return subjectType == SubjectType.WORKS ? deMinimisWorks : deMinimisGoodsServices;
        }
    }

    public record SimpleProcurement(LocalDate validFrom, LocalDate validTo, BigDecimal goodsServices, BigDecimal works,
                                    String source) implements Period {
    }

    /** JavniRadar heuristics for rule D, not legal thresholds (docs/ANOMALY_DETECTION.md §D). */
    public record RuleD(BigDecimal minInitialValueEur, BigDecimal overpaymentShare, BigDecimal overpaymentMinEur,
                        BigDecimal implausibleRatio, String source) {
    }

    public interface Period {
        LocalDate validFrom();

        LocalDate validTo();

        /** An empty bound means the period is open on that side. */
        default boolean covers(LocalDate date) {
            return (validFrom() == null || !date.isBefore(validFrom())) && (validTo() == null || !date.isAfter(validTo()));
        }
    }

    public Optional<ModificationLimits> modificationLimitsOn(LocalDate dateSigned) {
        return contractModification.stream().filter(period -> period.covers(dateSigned)).findFirst();
    }
}
