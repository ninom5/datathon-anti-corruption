package com.datathon.api.contract;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContractRepository extends JpaRepository<Contract, Long>, JpaSpecificationExecutor<Contract> {

    Optional<Contract> findByNaturalKey(String naturalKey);

    @Query("""
            select c from Contract c
            join fetch c.buyer
            left join fetch c.supplier
            join fetch c.provenance p
            left join fetch p.rawRecord r
            left join fetch r.file f
            left join fetch f.dataset""")
    List<Contract> findAllWithSource();

    @Query("""
            select c from Contract c
            join fetch c.buyer
            left join fetch c.supplier
            join fetch c.provenance p
            left join fetch p.rawRecord r
            left join fetch r.file f
            left join fetch f.dataset
            where c.id = :id""")
    Optional<Contract> findDetailById(@Param("id") Long id);

    @Override
    @EntityGraph(attributePaths = {"buyer", "supplier"})
    Page<Contract> findAll(Specification<Contract> specification, Pageable pageable);

    @EntityGraph(attributePaths = {"supplier"})
    List<Contract> findByIdInOrderByDateSignedAscIdAsc(java.util.Collection<Long> ids);

    long countByIsSyntheticFalse();

    @Query("""
            select count(c) > 0 from Contract c join c.buyer b
            where c.eojnContractUrl is not null and c.isSynthetic = false
              and c.dateSigned between :from and :to
              and (b.oib = :buyerOib or b.nameNorm = :buyerNameNorm)""")
    boolean existsClassAForBuyerBetween(@Param("buyerOib") String buyerOib, @Param("buyerNameNorm") String buyerNameNorm,
                                         @Param("from") java.time.LocalDate from, @Param("to") java.time.LocalDate to);

    @Query("""
            select year(c.dateSigned), count(c), sum(coalesce(c.totalValue, c.initialValue)) from Contract c
            left join c.supplier s
            where c.buyer.id = :buyerId and c.dateSigned is not null
              and (s.id is null or s.orgType <> com.datathon.api.organization.OrganizationType.NATURAL_PERSON)
            group by year(c.dateSigned) order by year(c.dateSigned)""")
    List<Object[]> sumByYearForBuyer(@Param("buyerId") Long buyerId);

    @Query("""
            select substring(c.cpv, 1, 2), count(c), sum(coalesce(c.totalValue, c.initialValue)) from Contract c
            left join c.supplier s
            where c.buyer.id = :buyerId and c.cpv is not null
              and (s.id is null or s.orgType <> com.datathon.api.organization.OrganizationType.NATURAL_PERSON)
            group by substring(c.cpv, 1, 2) order by sum(coalesce(c.totalValue, c.initialValue)) desc nulls last""")
    List<Object[]> sumByCpvForBuyer(@Param("buyerId") Long buyerId);

    @Query("""
            select s, count(c), sum(coalesce(c.totalValue, c.initialValue)) from Contract c join c.supplier s
            where c.buyer.id = :buyerId
              and s.orgType not in (com.datathon.api.organization.OrganizationType.NATURAL_PERSON,
                                    com.datathon.api.organization.OrganizationType.CRAFT)
            group by s order by sum(coalesce(c.totalValue, c.initialValue)) desc nulls last""")
    List<Object[]> sumBySupplierForBuyer(@Param("buyerId") Long buyerId);

    @Query("""
            select count(c) from Contract c join c.supplier s
            where c.buyer.id = :buyerId and s.orgType = com.datathon.api.organization.OrganizationType.CRAFT""")
    long countCraftContractsForBuyer(@Param("buyerId") Long buyerId);

    @Query("""
            select d.title, d.landingUrl, f.formatClass, count(distinct f.id), count(c) from Contract c
            join c.provenance p join p.rawRecord r join r.file f join f.dataset d
            left join c.supplier s
            where c.buyer.id = :buyerId
              and (s.id is null or s.orgType <> com.datathon.api.organization.OrganizationType.NATURAL_PERSON)
            group by d.title, d.landingUrl, f.formatClass order by count(c) desc""")
    List<Object[]> sourcesForBuyer(@Param("buyerId") Long buyerId);
}
