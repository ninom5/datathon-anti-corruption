package com.datathon.api.municipality;

import com.datathon.api.common.NotFoundException;
import com.datathon.api.municipality.dto.MunicipalityOverview;
import com.datathon.api.municipality.dto.PeerComparison;
import com.datathon.api.municipality.dto.PeerComparison.CategoryOption;
import com.datathon.api.municipality.dto.PeerComparison.Member;
import com.datathon.api.municipality.dto.PeerComparison.YearOption;
import com.datathon.api.reference.Municipality;
import com.datathon.api.reference.MunicipalityRepository;
import com.datathon.api.reference.PeerGroups;
import com.datathon.api.source.DataCoverage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** All JLS for the map, and one JLS compared with its peer group (docs/ANOMALY_DETECTION.md §3A). */
@Service
@RequiredArgsConstructor
public class MunicipalityService {

    /** n_peer_min from docs/ANOMALY_DETECTION.md §3A. */
    static final int MINIMUM_FOR_COMPARISON = 8;

    private final MunicipalityStatsRepository statsRepository;
    private final MunicipalityRepository municipalityRepository;
    private final DataCoverage dataCoverage;

    @Transactional(readOnly = true)
    public List<MunicipalityOverview> listAll() {
        return statsRepository.overviewOfAll().stream().map(MunicipalityService::toOverview).toList();
    }

    /**
     * @param year null = the newest complete year with data in the group
     * @param cpv2 null = all categories
     */
    @Transactional(readOnly = true)
    public PeerComparison comparePeers(String oib, Integer year, String cpv2) {
        Municipality municipality = municipalityRepository.findById(oib)
                .orElseThrow(() -> new NotFoundException("JLS s OIB-om " + oib + " nije u popisu jedinica lokalne samouprave."));
        String peerGroup = municipality.getPeerGroup();
        if (peerGroup == null) {
            throw new NotFoundException(municipality.getName() + " nema usporednu skupinu (nema podatka o broju stanovnika).");
        }
        int incompleteFromYear = dataCoverage.incompleteFromYear();
        List<YearOption> years = statsRepository.yearsWithData(peerGroup).stream()
                .map(row -> new YearOption(number(row[0]).intValue(), number(row[1]).intValue(),
                        number(row[0]).intValue() >= incompleteFromYear))
                .toList();
        int selectedYear = year != null ? year : defaultYear(years, incompleteFromYear);
        List<Member> members = members(oib, peerGroup, selectedYear, cpv2);
        Integer rank = rankOf(members);
        return new PeerComparison(
                new PeerComparison.Municipality(municipality.getOib(), municipality.getName(), municipality.getKind(),
                        municipality.getCounty(), municipality.getPopulation2021()),
                peerGroup, PeerGroups.labelOf(peerGroup), selectedYear, selectedYear >= incompleteFromYear, years,
                cpv2, cpv2 == null ? null : CpvDivisions.nameOf(cpv2), categories(peerGroup, selectedYear),
                statsRepository.countInPeerGroup(peerGroup), members.size(), MINIMUM_FOR_COMPARISON,
                members.size() >= MINIMUM_FOR_COMPARISON, median(members), rank, members);
    }

    private static MunicipalityOverview toOverview(Object[] row) {
        long contracts = number(row[6]).longValue();
        long worthChecking = number(row[9]).longValue();
        BigDecimal per100 = contracts == 0 ? null
                : BigDecimal.valueOf(worthChecking * 100).divide(BigDecimal.valueOf(contracts), 1, RoundingMode.HALF_UP);
        return new MunicipalityOverview((String) row[0], (String) row[1], (String) row[2], (String) row[3],
                row[4] == null ? null : number(row[4]).intValue(), (String) row[5], contracts,
                row[7] == null ? null : number(row[7]).intValue(), row[8] == null ? null : number(row[8]).intValue(),
                worthChecking, number(row[10]).longValue(), per100);
    }

    /**
     * A year that was not over at retrieval has partial totals, so it is not the default (DataCoverage). A group
     * without any contract gets the last complete year and an empty comparison, which the UI shows as such.
     */
    private static int defaultYear(List<YearOption> years, int incompleteFromYear) {
        if (years.isEmpty()) {
            return incompleteFromYear - 1;
        }
        return years.stream().filter(option -> !option.incomplete()).findFirst()
                .orElse(years.getFirst()).year();
    }

    private List<Member> members(String selectedOib, String peerGroup, int year, String cpv2) {
        List<Member> members = new ArrayList<>();
        for (Object[] row : statsRepository.peerTotals(peerGroup, year, cpv2)) {
            Integer population = row[2] == null ? null : number(row[2]).intValue();
            BigDecimal value = row[5] == null ? BigDecimal.ZERO : (BigDecimal) row[5];
            BigDecimal perCapita = population == null || population == 0 ? null
                    : value.divide(BigDecimal.valueOf(population), 2, RoundingMode.HALF_UP);
            String oib = (String) row[0];
            members.add(new Member(oib, (String) row[1], population, number(row[3]).longValue(),
                    number(row[4]).longValue(), value, perCapita, oib.equals(selectedOib)));
        }
        members.sort(Comparator.comparing(Member::valuePerCapita, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Member::name));
        return members;
    }

    private static Integer rankOf(List<Member> members) {
        for (int index = 0; index < members.size(); index++) {
            if (members.get(index).selected()) {
                return index + 1;
            }
        }
        return null;
    }

    private static BigDecimal median(List<Member> members) {
        List<BigDecimal> values = members.stream().map(Member::valuePerCapita).filter(value -> value != null).sorted().toList();
        if (values.isEmpty()) {
            return null;
        }
        int middle = values.size() / 2;
        if (values.size() % 2 == 1) {
            return values.get(middle);
        }
        return values.get(middle - 1).add(values.get(middle)).divide(BigDecimal.TWO, 2, RoundingMode.HALF_UP);
    }

    private List<CategoryOption> categories(String peerGroup, int year) {
        return statsRepository.categoriesInYear(peerGroup, year).stream()
                .map(row -> new CategoryOption((String) row[0], CpvDivisions.nameOf((String) row[0]),
                        number(row[1]).intValue(), number(row[2]).longValue(),
                        row[3] == null ? BigDecimal.ZERO : (BigDecimal) row[3]))
                .toList();
    }

    private static Number number(Object value) {
        return (Number) value;
    }
}
