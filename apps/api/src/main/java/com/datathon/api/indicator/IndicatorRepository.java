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

    @Override
    @EntityGraph(attributePaths = {"buyer", "contract"})
    Page<Indicator> findAll(Specification<Indicator> specification, Pageable pageable);

    @EntityGraph(attributePaths = {"buyer", "contract"})
    Optional<Indicator> findDetailById(Long id);

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

    @Query("select i.id, cid from Indicator i join i.contractIds cid where i.id in :indicatorIds order by i.id, cid")
    List<Object[]> findContractIdPairs(@Param("indicatorIds") Collection<Long> indicatorIds);

    default List<Indicator> findActiveByContractId(Long contractId) {
        return findActiveByContractIds(List.of(contractId)).stream().map(ContractIndicator::indicator).toList();
    }

    @Query("""
            select i from Indicator i left join fetch i.contract
            where i.buyer.id = :buyerId and i.status = com.datathon.api.indicator.IndicatorStatus.ACTIVE
            order by i.ruleCode, i.magnitudeValue desc nulls last, i.id""")
    List<Indicator> findActiveByBuyer(@Param("buyerId") Long buyerId);

    @Modifying
    @Query("delete from Indicator i where i.ruleCode in :ruleCodes and i.ruleVersion = :ruleVersion")
    int deleteByRules(@Param("ruleCodes") List<String> ruleCodes, @Param("ruleVersion") String ruleVersion);

    @Query("select count(i) from Indicator i where i.ruleCode = :ruleCode and i.buyer.isSynthetic = false")
    long countRealByRuleCode(@Param("ruleCode") String ruleCode);
}
