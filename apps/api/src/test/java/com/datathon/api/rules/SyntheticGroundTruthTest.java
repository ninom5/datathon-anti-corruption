package com.datathon.api.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.TestcontainersConfiguration;
import com.datathon.api.contract.Contract;
import com.datathon.api.contract.ContractRepository;
import com.datathon.api.indicator.Indicator;
import com.datathon.api.indicator.IndicatorRepository;
import com.datathon.api.ingest.IngestIssueRepository;
import com.datathon.api.ingest.IssueCode;
import com.datathon.api.ingest.RegisterImporter;
import com.datathon.api.ingest.SheetReader;
import com.datathon.api.ingest.synthetic.SyntheticRegisterLoader;
import com.datathon.api.organization.OrganizationRepository;
import com.datathon.api.source.FormatClass;
import com.datathon.api.source.SourceDataset;
import com.datathon.api.source.SourceDatasetRepository;
import com.datathon.api.source.SourceFile;
import com.datathon.api.source.SourceFileRepository;
import com.datathon.api.wording.ForbiddenTerms;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * Ground truth for rules D, F and C: the synthetic register goes through the real XLSX reader, parser and rules, and every row
 * is compared with {@code fixtures/synthetic/expected.csv}. Prints a TP/FP/TN/FN matrix. Fails on any miss among
 * clear and data-quality cases; edge cases are only reported. Also checks that synthetic rows never change the
 * statistics of real data (a real register, Grad Osijek, is imported first).
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class SyntheticGroundTruthTest {

    private static final Logger log = LoggerFactory.getLogger(SyntheticGroundTruthTest.class);
    private static final String URL_PREFIX = "https://primjerovo.invalid/contract-eo/";
    private static final Set<String> STRICT_CATEGORIES = Set.of("jasno_pozitivan", "jasno_negativan", "kvaliteta");

    @Autowired
    private SyntheticRegisterLoader syntheticLoader;
    @Autowired
    private RegisterImporter registerImporter;
    @Autowired
    private RulesService rulesService;
    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private IndicatorRepository indicatorRepository;
    @Autowired
    private IngestIssueRepository issueRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private SourceDatasetRepository datasetRepository;
    @Autowired
    private SourceFileRepository fileRepository;

    record Expected(String ref, String category, String expected, String dataQuality, String severity, String evidence,
                    String reason) {

        /** Contracts of one F or C case share the first three characters of their reference (F01A, F01B, ...). */
        boolean isGroupCase() {
            return ref.startsWith("F") || ref.startsWith("C");
        }

        String rule() {
            return ref.substring(0, 1);
        }

        /**
         * A row is judged only by the rule it was written for (S = D1/D2, F, C). Other rules may also fire on the
         * same contracts (e.g. the D rows of Općina Primjerovo form a concentrated CPV cell); they are reported, not
         * counted as false positives.
         */
        boolean isJudged(String ruleCode) {
            if (!isGroupCase()) {
                return ruleCode.startsWith("D_");
            }
            return ruleCode.equals(rule().equals("F") ? SplittingRule.RULE_CODE : ConcentrationRule.RULE_CODE);
        }
    }

    record Outcome(Expected expected, String verdict, String actual, String detail) {
    }

    @Test
    void rulesMatchGroundTruthAndSyntheticStaysOutOfRealStatistics() throws IOException {
        importRealRegister();
        rulesService.runRules();
        RealStats before = realStats();

        syntheticLoader.load();

        assertThat(realStats()).as("sintetika ne smije mijenjati statistiku stvarnih podataka").isEqualTo(before);
        assertThat(organizationRepository.findByOib("00000000001")).get().matches(organization -> organization.isSynthetic());

        List<Outcome> outcomes = readExpected().stream().map(this::judge).toList();
        log.info("\n{}", report(outcomes));

        List<Outcome> strictMisses = outcomes.stream()
                .filter(outcome -> STRICT_CATEGORIES.contains(outcome.expected().category()) && !outcome.verdict().startsWith("OK"))
                .toList();
        assertThat(strictMisses).as("promašaji među jasnim slučajevima i slučajevima kvalitete").isEmpty();
        assertThat(count(outcomes, "jasno_pozitivan")).isGreaterThanOrEqualTo(5);
        assertThat(count(outcomes, "jasno_negativan")).isGreaterThanOrEqualTo(5);
        assertThat(count(outcomes, "rubni")).isGreaterThanOrEqualTo(5);
        assertThat(count(outcomes, "kvaliteta")).isGreaterThanOrEqualTo(2);
        for (String rule : List.of("F", "C")) {
            assertThat(outcomes.stream().filter(outcome -> outcome.expected().isGroupCase() && outcome.expected().rule().equals(rule)
                    && outcome.expected().category().equals("jasno_pozitivan")).count()).as("jasno pozitivni za " + rule).isGreaterThanOrEqualTo(2);
        }
        assertAllIndicatorTextsAreNeutral();
        assertThat(magnitudeOf("S031", ValueIncreaseRule.RULE_CODE)).as("loša kvaliteta ne mijenja veličinu odstupanja")
                .isEqualByComparingTo("0.40");
    }

    private BigDecimal magnitudeOf(String ref, String ruleCode) {
        Contract contract = contractRepository.findByNaturalKey(URL_PREFIX + ref).orElseThrow();
        return indicatorRepository.findActiveByContractId(contract.getId()).stream()
                .filter(indicator -> indicator.getRuleCode().equals(ruleCode)).findFirst().orElseThrow().getMagnitudeValue();
    }

    private Outcome judge(Expected expected) {
        Optional<Contract> contract = contractRepository.findByNaturalKey(URL_PREFIX + expected.ref());
        if (expected.expected().equals("rejected")) {
            boolean isRejected = contract.isEmpty() && issueRepository.findAll().stream()
                    .anyMatch(issue -> IssueCode.UNREADABLE_DATE_SIGNED.equals(issue.getCode()));
            return new Outcome(expected, isRejected ? "OK (odbačen)" : "PROMAŠAJ", contract.isEmpty() ? "odbačen" : "uvezen", "");
        }
        assertThat(contract).as("ugovor %s", expected.ref()).isPresent();
        assertThat(contract.get().isSynthetic()).isTrue();
        List<Indicator> indicators = indicatorRepository.findActiveByContractId(contract.get().getId());
        String actual = indicators.isEmpty() ? "none" : indicators.stream().map(Indicator::getRuleCode).sorted().collect(Collectors.joining("+"));
        boolean isExpectedPositive = !expected.expected().equals("none");
        if (!isExpectedPositive) {
            boolean hasJudgedIndicator = indicators.stream().anyMatch(indicator -> expected.isJudged(indicator.getRuleCode()));
            return new Outcome(expected, hasJudgedIndicator ? "FP" : "OK TN", actual, "");
        }
        Optional<Indicator> match = indicators.stream().filter(indicator -> indicator.getRuleCode().equals(expected.expected())).findFirst();
        if (match.isEmpty()) {
            return new Outcome(expected, "FN", actual, "");
        }
        String quality = match.get().getDataQuality().code();
        String severity = match.get().getSeverityNote().code();
        String strength = match.get().getEvidenceStrength().code();
        String detail = "kvaliteta=" + quality + ", dokaz=" + strength + ", " + severity + ", pojas="
                + (match.get().getMagnitudeBand() == null ? "-" : match.get().getMagnitudeBand().code())
                + ", veličina=" + match.get().getMagnitudeLabel();
        boolean areDimensionsRight = quality.equals(expected.dataQuality()) && severity.equals(expected.severity())
                && (expected.evidence().isEmpty() || strength.equals(expected.evidence()));
        if (!areDimensionsRight) {
            return new Outcome(expected, "TP, krive dimenzije", actual, detail);
        }
        boolean isGroupRight = !expected.isGroupCase() || Set.copyOf(indicatorRepository.findContractIds(match.get().getId())).equals(caseContractIds(expected));
        return new Outcome(expected, isGroupRight ? "OK TP" : "TP, kriva skupina", actual, detail);
    }

    /** Every synthetic contract of the case, e.g. F01A, F01B and F01C for F01A. */
    private Set<Long> caseContractIds(Expected expected) {
        String prefix = URL_PREFIX + expected.ref().substring(0, 3);
        return contractRepository.findAll().stream()
                .filter(contract -> contract.getNaturalKey().startsWith(prefix))
                .map(Contract::getId)
                .collect(Collectors.toSet());
    }

    private static String report(List<Outcome> outcomes) {
        StringBuilder text = new StringBuilder("=== Pravila D, F i C na sintetičkom skupu (rules-v1) ===\n");
        Map<String, Map<String, Integer>> matrix = new TreeMap<>();
        for (Outcome outcome : outcomes) {
            String cell = outcome.verdict().replace("OK ", "");
            String row = (outcome.expected().isGroupCase() ? outcome.expected().rule() : "D") + " " + outcome.expected().category();
            matrix.computeIfAbsent(row, key -> new TreeMap<>()).merge(cell, 1, Integer::sum);
        }
        matrix.forEach((row, cells) -> text.append(String.format("%-20s %s%n", row, cells)));
        text.append("--- po zapisu ---\n");
        for (Outcome outcome : outcomes) {
            text.append(String.format("%s %-16s očekivano=%-16s stvarno=%-32s %-22s %s | %s%n", outcome.expected().ref(),
                    outcome.expected().category(), outcome.expected().expected(), outcome.actual(), outcome.verdict(),
                    outcome.detail(), outcome.expected().reason()));
        }
        return text.toString();
    }

    private void assertAllIndicatorTextsAreNeutral() {
        List<String> hits = new ArrayList<>();
        for (Indicator indicator : indicatorRepository.findAll()) {
            String text = indicator.getExplanationHr() + " " + indicator.getNotMeaningHr() + " " + indicator.getMagnitudeLabel();
            ForbiddenTerms.findIn(text).forEach(term -> hits.add(indicator.getId() + ": " + term));
        }
        assertThat(hits).isEmpty();
    }

    record RealStats(long contracts, long valueIncrease, long overpayment, long splitting, long concentration) {
    }

    private RealStats realStats() {
        return new RealStats(contractRepository.countByIsSyntheticFalse(),
                indicatorRepository.countRealByRuleCode(ValueIncreaseRule.RULE_CODE),
                indicatorRepository.countRealByRuleCode(OverpaymentRule.RULE_CODE),
                indicatorRepository.countRealByRuleCode(SplittingRule.RULE_CODE),
                indicatorRepository.countRealByRuleCode(ConcentrationRule.RULE_CODE));
    }

    private void importRealRegister() throws IOException {
        SourceDataset dataset = new SourceDataset(SourceDataset.PORTAL_DATA_GOV_HR, "test-grad-osijek");
        dataset.setTitle("Registar ugovora Grada Osijeka (test fixture)");
        dataset = datasetRepository.save(dataset);
        SourceFile file = new SourceFile(dataset, "https://data.gov.hr/test/grad-osijek-registar-ugovora.xlsx", Instant.now());
        file.setFormat("XLSX");
        file.setFormatClass(FormatClass.A);
        file = fileRepository.save(file);
        try (InputStream input = getClass().getResourceAsStream("/fixtures/eojn/grad-osijek-registar-ugovora.xlsx")) {
            registerImporter.importClassA(null, file, SheetReader.readWorkbook(input.readAllBytes()), false);
        }
        assertThat(contractRepository.countByIsSyntheticFalse()).isPositive();
    }

    private static List<Expected> readExpected() throws IOException {
        try (InputStream input = SyntheticGroundTruthTest.class.getResourceAsStream("/fixtures/synthetic/expected.csv")) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8).lines().skip(1)
                    .filter(line -> !line.isBlank())
                    .map(line -> line.split(";", -1))
                    .map(cells -> new Expected(cells[0], cells[1], cells[2], cells[3], cells[4], cells[5], cells[6]))
                    .toList();
        }
    }

    private static long count(List<Outcome> outcomes, String category) {
        return outcomes.stream().filter(outcome -> outcome.expected().category().equals(category)).count();
    }
}
