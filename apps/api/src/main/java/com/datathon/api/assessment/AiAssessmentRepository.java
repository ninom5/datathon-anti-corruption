package com.datathon.api.assessment;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AiAssessmentRepository extends JpaRepository<AiAssessment, Long> {

    @Query("""
            select a from AiAssessment a
            where a.contract.id = :contractId and a.model = :model and a.promptVersion = :promptVersion
              and a.inputHash = :inputHash""")
    Optional<AiAssessment> findCached(@Param("contractId") Long contractId, @Param("model") String model,
                                      @Param("promptVersion") String promptVersion, @Param("inputHash") String inputHash);

    @Query("""
            select a from AiAssessment a
            where a.contract.id = :contractId
              and a.status <> com.datathon.api.assessment.AiAssessmentStatus.SUPERSEDED
            order by a.createdAt desc, a.id desc
            limit 1""")
    Optional<AiAssessment> findLatest(@Param("contractId") Long contractId);

    @Query(nativeQuery = true, value = """
            select c.id, c.subject, coalesce(c.total_value_eur, c.initial_value_eur),
                   exists (select 1 from indicator_contract ic join indicator i on i.id = ic.indicator_id
                           where ic.contract_id = c.id and i.status = 'aktivan')
            from contract c join organization s on s.id = c.supplier_id
            where s.oib is not null and s.org_type not in ('obrt', 'fizicka_osoba', 'ostalo', 'nepoznato') and c.is_synthetic = :synthetic""")
    List<Object[]> findCandidateRows(@Param("synthetic") boolean synthetic);

    @Query(nativeQuery = true, value = """
            select c.id, b.name, c.subject, coalesce(c.total_value_eur, c.initial_value_eur), c.date_signed,
                   c.procedure_type, f.url, r.locator
            from contract c
            join organization b on b.id = c.buyer_id
            join organization s on s.id = c.supplier_id
            join provenance p on p.id = c.provenance_id
            join raw_record r on r.id = p.raw_record_id
            join source_file f on f.id = r.file_id
            where not c.is_synthetic and f.format_class = 'A'
              and s.oib is not null and s.org_type not in ('obrt', 'fizicka_osoba', 'ostalo', 'nepoznato')
            order by md5(c.id::text || :seed)
            limit :size""")
    List<Object[]> findRealSample(@Param("seed") String seed, @Param("size") int size);
}
