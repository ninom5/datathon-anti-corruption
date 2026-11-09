package com.datathon.api.organization;

import com.datathon.api.common.NotFoundException;
import com.datathon.api.contract.ContractRepository;
import com.datathon.api.indicator.Indicator;
import com.datathon.api.indicator.IndicatorRepository;
import com.datathon.api.indicator.IndicatorService;
import com.datathon.api.indicator.SeverityNote;
import com.datathon.api.indicator.dto.RuleTitles;
import com.datathon.api.organization.dto.BuyerProfile;
import com.datathon.api.organization.dto.BuyerProfile.Coverage;
import com.datathon.api.organization.dto.BuyerProfile.CpvTotal;
import com.datathon.api.organization.dto.BuyerProfile.Jpt;
import com.datathon.api.organization.dto.BuyerProfile.MunicipalityInfo;
import com.datathon.api.organization.dto.BuyerProfile.RuleCount;
import com.datathon.api.organization.dto.BuyerProfile.SourceRef;
import com.datathon.api.organization.dto.BuyerProfile.SupplierTotal;
import com.datathon.api.organization.dto.BuyerProfile.YearTotal;
import com.datathon.api.organization.dto.OrganizationRef;
import com.datathon.api.reference.MunicipalityRepository;
import com.datathon.api.source.DataCoverage;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BuyerProfileService {

    private static final int TOP_SUPPLIERS = 10;

    private final OrganizationRepository organizationRepository;
    private final ContractRepository contractRepository;
    private final IndicatorRepository indicatorRepository;
    private final IndicatorService indicatorService;
    private final MunicipalityRepository municipalityRepository;
    private final DataCoverage dataCoverage;

    @Transactional(readOnly = true)
    public BuyerProfile getByOib(String oib) {
        Organization buyer = organizationRepository.findByOib(oib)
                .filter(organization -> organization.getOrgType() != OrganizationType.NATURAL_PERSON)
                .orElseThrow(() -> new NotFoundException("Naručitelj s OIB-om " + oib + " ne postoji u podacima."));
        int incompleteFromYear = dataCoverage.incompleteFromYear();
        List<YearTotal> byYear = contractRepository.sumByYearForBuyer(buyer.getId()).stream()
                .map(row -> new YearTotal(((Number) row[0]).intValue(), (Long) row[1], money(row[2]),
                        ((Number) row[0]).intValue() >= incompleteFromYear))
                .toList();
        if (byYear.isEmpty()) {
            throw new NotFoundException("Naručitelj s OIB-om " + oib + " nema objavljenih ugovora u podacima.");
        }
        List<Indicator> indicators = indicatorRepository.findActiveByBuyer(buyer.getId());
        return new BuyerProfile(OrganizationRef.from(buyer), jptOf(buyer), municipalityOf(oib), coverage(byYear),
                byYear.stream().mapToLong(YearTotal::contracts).sum(),
                byYear.stream().map(YearTotal::totalValue).reduce(BigDecimal.ZERO, BigDecimal::add),
                byYear, cpvTotals(buyer), topSuppliers(buyer), contractRepository.countCraftContractsForBuyer(buyer.getId()),
                ruleCounts(indicators), indicatorService.toResponses(indicators), sources(buyer), buyer.isSynthetic());
    }

    private static Jpt jptOf(Organization buyer) {
        return buyer.getJptCode() == null ? null : new Jpt(buyer.getJptCode(), buyer.getJptType(), buyer.getCounty());
    }

    private MunicipalityInfo municipalityOf(String oib) {
        return municipalityRepository.findById(oib)
                .map(municipality -> new MunicipalityInfo(municipality.getName(), municipality.getKind(),
                        municipality.getCounty(), municipality.getPopulation2021(), municipality.getPeerGroup()))
                .orElse(null);
    }

    private static Coverage coverage(List<YearTotal> byYear) {
        List<Integer> years = byYear.stream().map(YearTotal::year).toList();
        List<Integer> incomplete = byYear.stream().filter(YearTotal::incomplete).map(YearTotal::year).toList();
        int from = years.getFirst();
        int to = years.getLast();
        StringBuilder label = new StringBuilder(from == to ? from + "." : from + ".–" + to + ".");
        if (years.size() < to - from + 1) {
            label.append(", bez ugovora u nekim godinama");
        }
        for (Integer year : incomplete) {
            label.append(", ").append(year).append(". nepotpuna");
        }
        return new Coverage(from, to, years, incomplete, label.toString());
    }

    private List<CpvTotal> cpvTotals(Organization buyer) {
        return contractRepository.sumByCpvForBuyer(buyer.getId()).stream()
                .map(row -> new CpvTotal((String) row[0], (Long) row[1], money(row[2])))
                .toList();
    }

    private List<SupplierTotal> topSuppliers(Organization buyer) {
        return contractRepository.sumBySupplierForBuyer(buyer.getId()).stream()
                .limit(TOP_SUPPLIERS)
                .map(row -> new SupplierTotal(OrganizationRef.from((Organization) row[0]), (Long) row[1], money(row[2])))
                .toList();
    }

    private static List<RuleCount> ruleCounts(List<Indicator> indicators) {
        Map<String, long[]> counts = new LinkedHashMap<>();
        for (Indicator indicator : indicators) {
            long[] count = counts.computeIfAbsent(indicator.getRuleCode(), key -> new long[2]);
            count[indicator.getSeverityNote() == SeverityNote.INFORMATIVE ? 1 : 0]++;
        }
        return counts.entrySet().stream()
                .map(entry -> new RuleCount(entry.getKey(), RuleTitles.of(entry.getKey()),
                        entry.getValue()[0] + entry.getValue()[1], entry.getValue()[0], entry.getValue()[1]))
                .toList();
    }

    private List<SourceRef> sources(Organization buyer) {
        return contractRepository.sourcesForBuyer(buyer.getId()).stream()
                .map(row -> new SourceRef((String) row[0], (String) row[1], row[2] == null ? null : row[2].toString(),
                        (Long) row[3], (Long) row[4]))
                .toList();
    }

    private static BigDecimal money(Object value) {
        return value == null ? BigDecimal.ZERO : (BigDecimal) value;
    }
}
