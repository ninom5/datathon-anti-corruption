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
                             RuleD ruleD, RuleF ruleF, RuleC ruleC, Bands bands) {

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

        /** Goods and services share one threshold; only works have their own (ZJN čl. 12 st. 1). */
        public BigDecimal limit(SubjectType subjectType) {
            return subjectType == SubjectType.WORKS ? works : goodsServices;
        }
    }

    /** JavniRadar heuristics for rule F (docs/ANOMALY_DETECTION.md §F). */
    public record RuleF(int windowDays, BigDecimal nearBand, BigDecimal maxShareFactor, int strongMinContracts,
                        List<String> recurringCpv2, List<String> publicProcedureMarkers, String source) {
    }

    /** JavniRadar heuristics for rule C (docs/ANOMALY_DETECTION.md §C). */
    public record RuleC(int periodYears, int minContracts, BigDecimal minTotalEur, int minDistinctYears,
                        BigDecimal top1Share, BigDecimal hhi, int strongMinContracts, int minTopContracts,
                        List<String> naturalMonopolyCpv2,
                        String source) {
    }

    /** Bounds of the magnitude band per rule; a heuristic for filters, never a combined score. */
    public record Bands(Band valueIncrease, Band overpayment, Band splitting, Band concentration) {
    }

    /** {@code strict}: the value must be above the bound; otherwise at or above it. */
    public record Band(BigDecimal mediumFrom, BigDecimal highFrom, boolean strict) {
    }

    /** JavniRadar heuristics for rule D, not legal thresholds (docs/ANOMALY_DETECTION.md §D). */
    public record RuleD(BigDecimal minInitialValueEur, BigDecimal overpaymentShare, BigDecimal overpaymentMinEur,
                        BigDecimal implausibleRatio, BigDecimal implausibleRatioWithoutNote,
                        BigDecimal currencyMixTolerance, String source) {
    }

    public interface Period {
        LocalDate validFrom();

        LocalDate validTo();

        /** An empty bound means the period is open on that side. */
        default boolean covers(LocalDate date) {
            return (validFrom() == null || !date.isBefore(validFrom())) && (validTo() == null || !date.isAfter(validTo()));
        }
    }

    public Optional<SimpleProcurement> simpleProcurementOn(LocalDate dateSigned) {
        return simpleProcurement.stream().filter(period -> period.covers(dateSigned)).findFirst();
    }

    public Optional<ModificationLimits> modificationLimitsOn(LocalDate dateSigned) {
        return contractModification.stream().filter(period -> period.covers(dateSigned)).findFirst();
    }
}
