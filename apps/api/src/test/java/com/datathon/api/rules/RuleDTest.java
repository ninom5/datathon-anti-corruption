package com.datathon.api.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.contract.Contract;
import com.datathon.api.contract.SubjectType;
import com.datathon.api.indicator.DataQuality;
import com.datathon.api.indicator.EvidenceStrength;
import com.datathon.api.indicator.SeverityNote;
import com.datathon.api.wording.ForbiddenTerms;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;

class RuleDTest {

    private static RuleThresholds thresholds;
    private static ValueIncreaseRule valueIncrease;
    private static OverpaymentRule overpayment;

    @BeforeAll
    static void loadThresholds() throws IOException {
        StandardEnvironment environment = new StandardEnvironment();
        new YamlPropertySourceLoader().load("thresholds", new ClassPathResource("config/thresholds.yaml"))
                .forEach(environment.getPropertySources()::addLast);
        thresholds = Binder.get(environment).bind("javniradar.thresholds", RuleThresholds.class).get();
        valueIncrease = new ValueIncreaseRule(thresholds);
        overpayment = new OverpaymentRule(thresholds);
    }

    @Test
    void thresholdsAreChosenBySigningDate() {
        assertThat(thresholds.modificationLimitsOn(LocalDate.of(2026, 5, 15)).orElseThrow().validTo()).isEqualTo(LocalDate.of(2026, 5, 15));
        assertThat(thresholds.modificationLimitsOn(LocalDate.of(2026, 5, 16)).orElseThrow().validFrom()).isEqualTo(LocalDate.of(2026, 5, 16));
        assertThat(thresholds.modificationLimitsOn(LocalDate.of(2026, 5, 16)).orElseThrow().deMinimis(SubjectType.WORKS)).isEqualByComparingTo("0.15");
        assertThat(thresholds.ruleD().minInitialValueEur()).isEqualByComparingTo("10000");
    }

    @Test
    void positiveIncreaseAboveDeMinimis() {
        Contract contract = contract(SubjectType.WORKS, "200000", "350000");
        var draft = valueIncrease.evaluate(contract).orElseThrow();
        assertThat(draft.magnitudeValue()).isEqualByComparingTo("0.75");
        assertThat(draft.magnitudeLabel()).isEqualTo("+75 % u odnosu na početni iznos");
        assertThat(draft.dataQuality()).isEqualTo(DataQuality.HIGH);
        assertThat(draft.evidenceStrength()).isEqualTo(EvidenceStrength.MEDIUM);
        assertThat(draft.severityNote()).isEqualTo(SeverityNote.WORTH_CHECKING);
        assertThat(draft.explanationHr()).contains("200.000,00 €", "350.000,00 €", "15 % za radove");
        assertNoForbiddenWords(draft);
    }

    @Test
    void negativeIncreaseWithinDeMinimisOrSmallContractOrFramework() {
        assertThat(valueIncrease.evaluate(contract(SubjectType.WORKS, "100000", "114000"))).isEmpty();
        assertThat(valueIncrease.evaluate(contract(SubjectType.GOODS, "9000", "12000"))).isEmpty();
        Contract framework = contract(SubjectType.GOODS, "60000", "96000");
        framework.setFrameworkBased(true);
        assertThat(valueIncrease.evaluate(framework)).isEmpty();
        Contract callOff = contract(SubjectType.GOODS, "60000", "96000");
        callOff.setSubject("Tromjesečna evidencija ugovora: Opskrba električnom energijom");
        assertThat(valueIncrease.evaluate(callOff)).as("EOJN quarterly record of framework call-offs").isEmpty();
    }

    @Test
    void edgeExactlyAtThresholdIsNotFlaggedJustAboveIs() {
        assertThat(valueIncrease.evaluate(contract(SubjectType.GOODS, "100000", "110000"))).isEmpty();
        assertThat(valueIncrease.evaluate(contract(SubjectType.GOODS, "100000", "110100"))).isPresent();
    }

    @Test
    void qualityFlagLowersDataQualityButNotMagnitude() {
        Contract contract = contract(SubjectType.WORKS, "100000", "140000");
        contract.setQualityFlags(List.of("supplier_oib_malformed"));
        var draft = valueIncrease.evaluate(contract).orElseThrow();
        assertThat(draft.dataQuality()).isEqualTo(DataQuality.MEDIUM);
        assertThat(draft.magnitudeValue()).isEqualByComparingTo("0.40");
    }

    @Test
    void overpaymentPositiveNegativeAndEdge() {
        assertThat(overpayment.evaluate(paid("37500", "52000"))).isPresent();
        assertThat(overpayment.evaluate(paid("31250", "20000"))).isEmpty();
        assertThat(overpayment.evaluate(paid("50000", "55000"))).as("točno +10 % i 5.000 EUR").isEmpty();
        assertThat(overpayment.evaluate(paid("20000", "23000"))).as("+15 %, ali samo 3.000 EUR").isEmpty();
        assertThat(overpayment.evaluate(paid("20000", null))).isEmpty();
    }

    @Test
    void implausibleOverpaymentIsInformativeWithLowQuality() {
        var draft = overpayment.evaluate(paid("4214.93", "516902")).orElseThrow();
        assertThat(draft.dataQuality()).isEqualTo(DataQuality.LOW);
        assertThat(draft.evidenceStrength()).isEqualTo(EvidenceStrength.WEAK);
        assertThat(draft.severityNote()).isEqualTo(SeverityNote.INFORMATIVE);
        assertThat(draft.explanationHr()).contains("pogrešku pri unosu");
        assertNoForbiddenWords(draft);
    }

    @Test
    void grossBelowNetIsADataErrorNotAnOverpayment() {
        Contract contract = contract(SubjectType.SERVICES, "7000", "7000");
        contract.setTotalValueGross(new BigDecimal("700"));
        contract.setPaidAmount(new BigDecimal("6863.60"));
        var draft = overpayment.evaluate(contract).orElseThrow();
        assertThat(draft.dataQuality()).isEqualTo(DataQuality.LOW);
        assertThat(draft.severityNote()).isEqualTo(SeverityNote.INFORMATIVE);
        assertThat(draft.explanationHr()).contains("manji od iznosa bez PDV-a");
    }

    @Test
    void kunaEuroRatioIsInformative() {
        Contract contract = contract(SubjectType.SERVICES, "8746.43", "8746.43");
        contract.setTotalValue(null);
        contract.setInitialValueGross(new BigDecimal("10933.04"));
        contract.setPaidAmount(new BigDecimal("82375.00"));
        var draft = overpayment.evaluate(contract).orElseThrow();
        assertThat(draft.dataQuality()).isEqualTo(DataQuality.LOW);
        assertThat(draft.severityNote()).isEqualTo(SeverityNote.INFORMATIVE);
        assertThat(draft.explanationHr()).contains("tečaju kune i eura");
        assertNoForbiddenWords(draft);

        Contract explained = paid("10000", "70000");
        explained.setPaymentNote("Dodatne isporuke po narudžbenicama");
        var nearRate = overpayment.evaluate(explained).orElseThrow();
        assertThat(nearRate.severityNote()).as("7,0x is outside the 1,5 % tolerance").isEqualTo(SeverityNote.WORTH_CHECKING);
        assertThat(nearRate.explanationHr()).doesNotContain("tečaju kune i eura");
    }

    @Test
    void largeOverpaymentWithoutNoteIsInformative() {
        var draft = overpayment.evaluate(paid("10000", "60000")).orElseThrow();
        assertThat(draft.dataQuality()).isEqualTo(DataQuality.LOW);
        assertThat(draft.severityNote()).isEqualTo(SeverityNote.INFORMATIVE);
        assertThat(draft.explanationHr()).contains("ne navodi obrazloženje");
        assertNoForbiddenWords(draft);
    }

    private static Contract contract(SubjectType type, String initial, String total) {
        Contract contract = new Contract("https://primjerovo.invalid/contract-eo/T");
        contract.setSubjectType(type);
        contract.setSubject("Test");
        contract.setDateSigned(LocalDate.of(2025, 3, 1));
        contract.setInitialValue(new BigDecimal(initial));
        contract.setTotalValue(new BigDecimal(total));
        return contract;
    }

    private static Contract paid(String totalGross, String paid) {
        Contract contract = contract(SubjectType.SERVICES, "1000", "1000");
        contract.setTotalValueGross(new BigDecimal(totalGross));
        contract.setPaidAmount(paid == null ? null : new BigDecimal(paid));
        return contract;
    }

    private static void assertNoForbiddenWords(IndicatorDraft draft) {
        assertThat(ForbiddenTerms.findIn(draft.explanationHr() + " " + draft.notMeaningHr() + " " + draft.magnitudeLabel())).isEmpty();
    }
}
