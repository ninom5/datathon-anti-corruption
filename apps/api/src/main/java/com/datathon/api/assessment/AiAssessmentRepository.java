package com.datathon.api.assessment;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AiAssessmentRepository extends JpaRepository<AiAssessment, Long> {

    /** Cache lookup: the same contract, model, prompt version and context were already assessed (§4.1). */
    @Query("""
            select a from AiAssessment a
            where a.contract.id = :contractId and a.model = :model and a.promptVersion = :promptVersion
              and a.inputHash = :inputHash""")
    Optional<AiAssessment> findCached(@Param("contractId") Long contractId, @Param("model") String model,
                                      @Param("promptVersion") String promptVersion, @Param("inputHash") String inputHash);

    /**
     * Newest assessment of a contract that was not replaced by a recheck, accepted or not. Only an accepted one is shown:
     * when the newest answer for the current data was rejected, an older accepted one is stale and must not be shown.
     */
    @Query("""
            select a from AiAssessment a
            where a.contract.id = :contractId
              and a.status <> com.datathon.api.assessment.AiAssessmentStatus.SUPERSEDED
            order by a.createdAt desc, a.id desc
            limit 1""")
    Optional<AiAssessment> findLatest(@Param("contractId") Long contractId);

    /**
     * {@code [contractId, subject, value, hasActiveIndicator]} of contracts that may be sent to the model: the supplier
     * has an OIB and is not a craft, a natural person, "ostalo" or "nepoznato" (the last two hold crafts with personal
     * names that the classifier did not recognise; GDPR, AGENTS.md). The value is total or initial EUR.
     */
    @Query(nativeQuery = true, value = """
            select c.id, c.subject, coalesce(c.total_value_eur, c.initial_value_eur),
                   exists (select 1 from indicator_contract ic join indicator i on i.id = ic.indicator_id
                           where ic.contract_id = c.id and i.status = 'aktivan')
            from contract c join organization s on s.id = c.supplier_id
            where s.oib is not null and s.org_type not in ('obrt', 'fizicka_osoba', 'ostalo', 'nepoznato') and c.is_synthetic = :synthetic""")
    List<Object[]> findCandidateRows(@Param("synthetic") boolean synthetic);

    /**
     * {@code [id, buyer, subject, value, dateSigned, procedureType, fileUrl, locator]}: a reproducible random sample of
     * real class A contracts with suppliers that may be sent to the model (docs/ANOMALY_DETECTION.md §4.6). The order
     * is md5(id + seed), so the same seed always gives the same sample.
     */
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
