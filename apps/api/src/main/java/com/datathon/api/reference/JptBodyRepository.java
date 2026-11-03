package com.datathon.api.reference;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JptBodyRepository extends JpaRepository<JptBody, String> {

    List<JptBody> findByNameNorm(String nameNorm);

    List<JptBody> findByJptTypeIn(List<String> jptTypes);

    /** True when the JPT register lists {@code founderOib} among the founders of {@code bodyOib}. */
    @Query(value = """
            select exists (select 1 from jpt_body b
                           where b.oib = :bodyOib
                             and b.founders @> jsonb_build_array(jsonb_build_object('oib', cast(:founderOib as text))))""",
            nativeQuery = true)
    boolean isFoundedBy(@Param("bodyOib") String bodyOib, @Param("founderOib") String founderOib);

    /**
     * Copies JPT code, type, founders and county to every organization with the same OIB, and marks JLS and
     * counties as such (the name classifier only guesses them from "Grad"/"Općina" in the name).
     */
    @Modifying
    @Query(value = """
            update organization o
               set jpt_code = b.jpt_code, jpt_type = b.jpt_type, founders = b.founders, county = b.county,
                   org_type = case
                       when b.jpt_type like '%samouprave - Općina' or b.jpt_type like '%samouprave - Grad'
                         or b.jpt_type like '%samouprave - Grad Zagreb' then 'jls'
                       when b.jpt_type like '%samouprave - Županija' then 'zupanija'
                       when o.org_type in ('nepoznato', 'ostalo') then 'javno_tijelo'
                       else o.org_type end
              from jpt_body b
             where b.oib = o.oib and not o.is_synthetic""", nativeQuery = true)
    int enrichOrganizations();
}
