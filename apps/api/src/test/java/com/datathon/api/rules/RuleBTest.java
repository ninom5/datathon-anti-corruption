package com.datathon.api.rules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.datathon.api.contract.Contract;
import com.datathon.api.indicator.MagnitudeBand;
import com.datathon.api.organization.CompanySnapshot;
import com.datathon.api.organization.CompanySnapshotRepository;
import com.datathon.api.organization.Organization;
import com.datathon.api.organization.OrganizationType;
import com.datathon.api.wording.ForbiddenTerms;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

/** Rule B on hand-built contracts and snapshots, with the real config/thresholds.yaml. */
class RuleBTest {

    private static RuleThresholds thresholds;
    private static final LocalDate SIGNED = LocalDate.of(2025, 6, 1);

    @BeforeAll
    static void loadThresholds() throws IOException {
        StandardEnvironment environment = new StandardEnvironment();
        new YamlPropertySourceLoader().load("thresholds", new ClassPathResource("config/thresholds.yaml"))
                .forEach(source -> environment.getPropertySources().addLast(source));
        thresholds = Binder.get(environment).bind("javniradar.thresholds", RuleThresholds.class).get();
    }

    private static Contract contract(OrganizationType type, String value) {
        Organization supplier = new Organization("00000000099", "Primjer nova d.o.o.", "primjer nova", type);
        ReflectionTestUtils.setField(supplier, "id", 99L);
        Contract contract = new Contract("k");
        contract.setSupplier(supplier);
        contract.setDateSigned(SIGNED);
        contract.setTotalValue(new BigDecimal(value));
        return contract;
    }

    private static YoungCompanyRule rule(LocalDate foundedOn) {
        CompanySnapshotRepository repository = mock(CompanySnapshotRepository.class);
        CompanySnapshot snapshot = mock(CompanySnapshot.class);
        when(snapshot.getFoundedOn()).thenReturn(foundedOn);
        when(snapshot.isNotFound()).thenReturn(false);
        when(snapshot.getRetrievedAt()).thenReturn(Instant.parse("2026-11-05T10:00:00Z"));
        when(repository.findLatest(any())).thenReturn(Optional.of(snapshot));
        return new YoungCompanyRule(thresholds, repository);
    }

    @Test
    void youngCompanyWithLargeContractIsFlagged() {
        IndicatorDraft draft = rule(SIGNED.minusDays(51)).evaluate(contract(OrganizationType.LLC, "200000")).orElseThrow();

        assertThat(draft.magnitudeValue()).isEqualByComparingTo("51");
        assertThat(draft.magnitudeLabel()).isEqualTo("osnovana 51 dan prije ugovora");
        assertThat(draft.explanationHr()).contains("Primjer nova d.o.o.").contains("51 dan kasnije");
        assertThat(draft.evidence()).anyMatch(link -> link.url().equals(YoungCompanyRule.SUDREG_URL));
        assertThat(ForbiddenTerms.findIn(draft.explanationHr() + draft.notMeaningHr())).isEmpty();
        assertThat(new MagnitudeBands(thresholds).of(YoungCompanyRule.RULE_CODE, draft.magnitudeValue()))
                .contains(MagnitudeBand.HIGH);
    }

    @Test
    void edgesOfAgeAndValue() {
        assertThat(rule(SIGNED.minusDays(180)).evaluate(contract(OrganizationType.LLC, "50000"))).isPresent();
        assertThat(rule(SIGNED.minusDays(181)).evaluate(contract(OrganizationType.LLC, "500000"))).isEmpty();
        assertThat(rule(SIGNED.minusDays(30)).evaluate(contract(OrganizationType.LLC, "49999.99"))).isEmpty();
        assertThat(new MagnitudeBands(thresholds).of(YoungCompanyRule.RULE_CODE, BigDecimal.valueOf(120)))
                .contains(MagnitudeBand.MEDIUM);
        assertThat(new MagnitudeBands(thresholds).of(YoungCompanyRule.RULE_CODE, BigDecimal.valueOf(150)))
                .contains(MagnitudeBand.LOW);
    }

    @Test
    void contractBeforeFoundingCraftsAndUnknownTypesAreSkipped() {
        assertThat(rule(SIGNED.plusDays(3)).evaluate(contract(OrganizationType.LLC, "200000"))).isEmpty();
        assertThat(rule(SIGNED.minusDays(10)).evaluate(contract(OrganizationType.CRAFT, "200000"))).isEmpty();
        assertThat(rule(SIGNED.minusDays(10)).evaluate(contract(OrganizationType.OTHER, "200000"))).isEmpty();
    }

    @Test
    void withoutSnapshotNothingIsComputed() {
        CompanySnapshotRepository repository = mock(CompanySnapshotRepository.class);
        when(repository.findLatest(any())).thenReturn(Optional.empty());

        assertThat(new YoungCompanyRule(thresholds, repository).evaluate(contract(OrganizationType.LLC, "200000")))
                .isEmpty();
    }
}
