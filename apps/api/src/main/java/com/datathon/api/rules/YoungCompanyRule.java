package com.datathon.api.rules;

import static com.datathon.api.rules.RuleSupport.eur;

import com.datathon.api.contract.Contract;
import com.datathon.api.indicator.DataQuality;
import com.datathon.api.indicator.EvidenceStrength;
import com.datathon.api.indicator.SeverityNote;
import com.datathon.api.organization.CompanySnapshot;
import com.datathon.api.organization.CompanySnapshotRepository;
import com.datathon.api.organization.OrganizationType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class YoungCompanyRule implements ContractRule {

    public static final String RULE_CODE = "B_YOUNG_COMPANY";
    static final String SUDREG_URL = "https://sudreg-data.gov.hr";

    static final String NOT_MEANING = "Nova tvrtka može biti nastavak postojećeg poslovanja ili imati iskusne osnivače "
            + "i stručnjake. Datum osnivanja sam po sebi ne govori ništa o kvaliteti ponude.";

    private static final Set<OrganizationType> REGISTERED_TYPES = Set.of(OrganizationType.LLC,
            OrganizationType.SIMPLE_LLC, OrganizationType.JOINT_STOCK, OrganizationType.PUBLIC_BODY);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy.");

    private final RuleThresholds thresholds;
    private final CompanySnapshotRepository snapshotRepository;

    @Override
    public String ruleCode() {
        return RULE_CODE;
    }

    @Override
    public Optional<IndicatorDraft> evaluate(Contract contract) {
        BigDecimal value = RuleSupport.valueOf(contract);
        if (contract.getSupplier() == null || !REGISTERED_TYPES.contains(contract.getSupplier().getOrgType())
                || contract.getDateSigned() == null || value == null
                || value.compareTo(thresholds.ruleB().minValueEur()) < 0) {
            return Optional.empty();
        }
        return snapshotRepository.findLatest(contract.getSupplier().getId())
                .filter(snapshot -> !snapshot.isNotFound() && snapshot.getFoundedOn() != null)
                .flatMap(snapshot -> evaluate(contract, value, snapshot));
    }

    private Optional<IndicatorDraft> evaluate(Contract contract, BigDecimal value, CompanySnapshot snapshot) {
        long ageDays = ChronoUnit.DAYS.between(snapshot.getFoundedOn(), contract.getDateSigned());
        if (ageDays < 0 || ageDays > thresholds.ruleB().maxAgeDays()) {
            return Optional.empty();
        }
        List<EvidenceLink> evidence = new ArrayList<>(RuleSupport.evidence(contract, List.of("Datum sklapanja",
                "Iznos bez PDV-a")));
        LocalDate retrievedOn = snapshot.getRetrievedAt().atZone(ZoneOffset.UTC).toLocalDate();
        evidence.add(new EvidenceLink("Sudski registar: datum osnivanja " + snapshot.getFoundedOn().format(DATE)
                + " (dohvaćeno " + retrievedOn.format(DATE) + ")", SUDREG_URL, "datum_osnivanja"));
        return Optional.of(new IndicatorDraft(RULE_CODE, BigDecimal.valueOf(ageDays), "dana",
                "osnovana " + ageDays + " " + days(ageDays) + " prije ugovora", RuleSupport.baseQuality(contract, false),
                EvidenceStrength.MEDIUM, SeverityNote.WORTH_CHECKING,
                explanation(contract, value, snapshot.getFoundedOn(), ageDays), NOT_MEANING,
                List.of("prethodnik tvrtke (pripajanje, podjela, promjena pravnog oblika)", "reference i osoblje ponuditelja",
                        "broj ponuda"),
                evidence));
    }

    private static String explanation(Contract contract, BigDecimal value, LocalDate foundedOn, long ageDays) {
        return RuleSupport.supplierName(contract.getSupplier()) + " upisan je u sudski registar " + foundedOn.format(DATE)
                + ", a ugovor vrijedan " + eur(value) + " bez PDV-a sklopljen je " + contract.getDateSigned().format(DATE)
                + " (" + ageDays + " " + days(ageDays) + " kasnije). Vrijedi provjeriti kako je ponuditelj dokazao "
                + "sposobnost (reference, osoblje).";
    }

    private static String days(long count) {
        long lastTwo = count % 100;
        return count % 10 == 1 && lastTwo != 11 ? "dan" : "dana";
    }
}
