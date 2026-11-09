package com.datathon.api.municipality;

import com.datathon.api.reference.Municipality;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface MunicipalityStatsRepository extends Repository<Municipality, String> {

    @Query(nativeQuery = true, value = """
            select m.oib, m.name, m.kind, m.county, m.population_2021, m.peer_group,
                   coalesce(cs.contracts, 0), cs.first_year, cs.last_year,
                   coalesce(ind.worth_checking, 0), coalesce(ind.informative, 0)
            from municipality m
            left join organization b on b.oib = m.oib and not b.is_synthetic
            left join lateral (
                select count(*) as contracts,
                       min(extract(year from c.date_signed))::int as first_year,
                       max(extract(year from c.date_signed))::int as last_year
                from contract c left join organization s on s.id = c.supplier_id
                where c.buyer_id = b.id and not c.is_synthetic
                  and (s.id is null or s.org_type <> 'fizicka_osoba')) cs on true
            left join lateral (
                select count(*) filter (where i.severity_note is distinct from 'informativno') as worth_checking,
                       count(*) filter (where i.severity_note = 'informativno') as informative
                from indicator i
                where i.buyer_id = b.id and i.status = 'aktivan') ind on true
            order by m.name""")
    List<Object[]> overviewOfAll();

    @Query(nativeQuery = true, value = """
            select m.oib, m.name, m.population_2021, count(c.id),
                   count(c.id) filter (where cast(:cpv2 as text) is null or substr(c.cpv, 1, 2) = :cpv2),
                   sum(coalesce(c.total_value_eur, c.initial_value_eur))
                       filter (where cast(:cpv2 as text) is null or substr(c.cpv, 1, 2) = :cpv2)
            from municipality m
            join organization b on b.oib = m.oib and not b.is_synthetic
            join contract c on c.buyer_id = b.id and not c.is_synthetic
                           and extract(year from c.date_signed) = :year
            left join organization s on s.id = c.supplier_id
            where m.peer_group = :peerGroup and (s.id is null or s.org_type <> 'fizicka_osoba')
            group by m.oib, m.name, m.population_2021""")
    List<Object[]> peerTotals(@Param("peerGroup") String peerGroup, @Param("year") int year, @Param("cpv2") String cpv2);

    @Query(nativeQuery = true, value = """
            select extract(year from c.date_signed)::int as year, count(distinct m.oib)
            from municipality m
            join organization b on b.oib = m.oib and not b.is_synthetic
            join contract c on c.buyer_id = b.id and not c.is_synthetic
            where m.peer_group = :peerGroup
            group by 1 order by 1 desc""")
    List<Object[]> yearsWithData(@Param("peerGroup") String peerGroup);

    @Query(nativeQuery = true, value = """
            select substr(c.cpv, 1, 2), count(distinct m.oib), count(c.id), sum(coalesce(c.total_value_eur, c.initial_value_eur))
            from municipality m
            join organization b on b.oib = m.oib and not b.is_synthetic
            join contract c on c.buyer_id = b.id and not c.is_synthetic
                           and extract(year from c.date_signed) = :year
            left join organization s on s.id = c.supplier_id
            where m.peer_group = :peerGroup and c.cpv is not null
              and (s.id is null or s.org_type <> 'fizicka_osoba')
            group by 1 order by 4 desc nulls last""")
    List<Object[]> categoriesInYear(@Param("peerGroup") String peerGroup, @Param("year") int year);

    @Query(nativeQuery = true, value = "select count(*) from municipality where peer_group = :peerGroup")
    int countInPeerGroup(@Param("peerGroup") String peerGroup);
}
