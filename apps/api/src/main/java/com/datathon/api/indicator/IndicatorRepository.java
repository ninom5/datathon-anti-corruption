package com.datathon.api.indicator;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IndicatorRepository extends JpaRepository<Indicator, Long>, JpaSpecificationExecutor<Indicator> {

    /** Indicator list page; buyer and contract (for the D2 source explanation) come with it. */
    @Override
    @EntityGraph(attributePaths = {"buyer", "contract"})
    Page<Indicator> findAll(Specification<Indicator> specification, Pageable pageable);

    /** One indicator for its detail page, with buyer and contract (D2 source explanation). */
    @EntityGraph(attributePaths = {"buyer", "contract"})
    Optional<Indicator> findDetailById(Long id);

    /** Active indicators about any of the contracts, including group indicators (F, C), with the contract they matched. */
    @Query("""
            select new com.datathon.api.indicator.IndicatorRepository$ContractIndicator(cid, i) from Indicator i
            join i.contractIds cid
            where cid in :contractIds and i.status = com.datathon.api.indicator.IndicatorStatus.ACTIVE
            order by i.ruleCode, i.id""")
    List<ContractIndicator> findActiveByContractIds(@Param("contractIds") Collection<Long> contractIds);

    record ContractIndicator(Long contractId, Indicator indicator) {
    }

    @Query("select cid from Indicator i join i.contractIds cid where i.id = :indicatorId order by cid")
    List<Long> findContractIds(@Param("indicatorId") Long indicatorId);

    /** Contract ids of many indicators in one query, as {@code [indicatorId, contractId]}. */
    @Query("select i.id, cid from Indicator i join i.contractIds cid where i.id in :indicatorIds order by i.id, cid")
    List<Object[]> findContractIdPairs(@Param("indicatorIds") Collection<Long> indicatorIds);

    default List<Indicator> findActiveByContractId(Long contractId) {
        return findActiveByContractIds(List.of(contractId)).stream().map(ContractIndicator::indicator).toList();
    }

    /** Active indicators of one buyer, largest deviation first within each rule (buyer profile). */
    @Query("""
            select i from Indicator i left join fetch i.contract
            where i.buyer.id = :buyerId and i.status = com.datathon.api.indicator.IndicatorStatus.ACTIVE
            order by i.ruleCode, i.magnitudeValue desc nulls last, i.id""")
    List<Indicator> findActiveByBuyer(@Param("buyerId") Long buyerId);

    /** Rules are recomputed as a whole; feedback states (osporen, ispravljen) do not exist yet in the POC. */
    @Modifying
    @Query("delete from Indicator i where i.ruleCode in :ruleCodes and i.ruleVersion = :ruleVersion")
    int deleteByRules(@Param("ruleCodes") List<String> ruleCodes, @Param("ruleVersion") String ruleVersion);

    /** Real data only: synthetic contracts never enter statistics (AGENTS.md). */
    @Query("select count(i) from Indicator i where i.ruleCode = :ruleCode and i.buyer.isSynthetic = false")
    long countRealByRuleCode(@Param("ruleCode") String ruleCode);
}
