package com.datathon.api.indicator;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IndicatorRepository extends JpaRepository<Indicator, Long> {

    List<Indicator> findByContractId(Long contractId);

    /** Rules are recomputed as a whole; feedback states (osporen, ispravljen) do not exist yet in the POC. */
    @Modifying
    @Query("delete from Indicator i where i.ruleCode in :ruleCodes and i.ruleVersion = :ruleVersion")
    int deleteByRules(@Param("ruleCodes") List<String> ruleCodes, @Param("ruleVersion") String ruleVersion);

    /** Real data only: synthetic contracts never enter statistics (AGENTS.md). */
    @Query("select count(i) from Indicator i where i.ruleCode = :ruleCode and i.contract.isSynthetic = false")
    long countRealByRuleCode(@Param("ruleCode") String ruleCode);
}
