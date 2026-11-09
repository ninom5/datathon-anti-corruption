package com.datathon.api.rules;

import com.datathon.api.contract.SubjectType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("javniradar.thresholds")
public record RuleThresholds(List<ModificationLimits> contractModification, List<SimpleProcurement> simpleProcurement,
                             RuleD ruleD, RuleF ruleF, RuleC ruleC, RuleB ruleB, Bands bands) {

    public record ModificationLimits(LocalDate validFrom, LocalDate validTo, BigDecimal deMinimisGoodsServices,
                                     BigDecimal deMinimisWorks, BigDecimal additionalSupplies,
                                     BigDecimal unforeseenCircumstances, String source) implements Period {

        public BigDecimal deMinimis(SubjectType subjectType) {
            return subjectType == SubjectType.WORKS ? deMinimisWorks : deMinimisGoodsServices;
        }
    }

    public record SimpleProcurement(LocalDate validFrom, LocalDate validTo, BigDecimal goodsServices, BigDecimal works,
                                    String source) implements Period {

        public BigDecimal limit(SubjectType subjectType) {
            return subjectType == SubjectType.WORKS ? works : goodsServices;
        }
    }

    public record RuleF(int windowDays, BigDecimal nearBand, BigDecimal maxShareFactor, int strongMinContracts,
                        List<String> recurringCpv2, List<String> publicProcedureMarkers, String source) {
    }

    public record RuleC(int periodYears, int minContracts, BigDecimal minTotalEur, int minDistinctYears,
                        BigDecimal top1Share, BigDecimal hhi, int strongMinContracts, int minTopContracts,
                        List<String> naturalMonopolyCpv2,
                        String source) {
    }

    public record RuleB(int maxAgeDays, BigDecimal minValueEur, String source) {
    }

    public record Bands(Band valueIncrease, Band overpayment, Band splitting, Band concentration, Band youngCompany) {
    }

    public record Band(BigDecimal mediumFrom, BigDecimal highFrom, boolean strict, boolean descending) {
    }

    public record RuleD(BigDecimal minInitialValueEur, BigDecimal overpaymentShare, BigDecimal overpaymentMinEur,
                        BigDecimal implausibleRatio, BigDecimal implausibleRatioWithoutNote,
                        BigDecimal currencyMixTolerance, String source) {
    }

    public interface Period {
        LocalDate validFrom();

        LocalDate validTo();

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
