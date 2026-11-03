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

    /** Everything a rule needs, in one query (contract, buyer, supplier and the source file of its row). */
    @Query("""
            select c from Contract c
            join fetch c.buyer
            left join fetch c.supplier
            join fetch c.provenance p
            left join fetch p.rawRecord r
            left join fetch r.file f
            left join fetch f.dataset""")
    List<Contract> findAllWithSource();

    /** One contract with buyer, supplier and the full provenance chain, for the detail page. */
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

    /** Explorer page; buyer and supplier are fetched with it to avoid one query per row. */
    @Override
    @EntityGraph(attributePaths = {"buyer", "supplier"})
    Page<Contract> findAll(Specification<Contract> specification, Pageable pageable);

    /** Contracts of an indicator group, oldest first, with their supplier. */
    @EntityGraph(attributePaths = {"supplier"})
    List<Contract> findByIdInOrderByDateSignedAscIdAsc(java.util.Collection<Long> ids);

    long countByIsSyntheticFalse();

    /**
     * True when the buyer already has standard EOJN export (class A) contracts signed in [from, to]. Used to skip
     * A2 rows of that buyer and year, because A2 has no EOJN link to deduplicate on (docs/DATA_MODEL.md §3).
     */
    @Query("""
            select count(c) > 0 from Contract c join c.buyer b
            where c.eojnContractUrl is not null and c.isSynthetic = false
              and c.dateSigned between :from and :to
              and (b.oib = :buyerOib or b.nameNorm = :buyerNameNorm)""")
    boolean existsClassAForBuyerBetween(@Param("buyerOib") String buyerOib, @Param("buyerNameNorm") String buyerNameNorm,
                                         @Param("from") java.time.LocalDate from, @Param("to") java.time.LocalDate to);

    // Buyer profile aggregates. Contracts with natural persons are never counted (GDPR); crafts are (numbers only).
    // The value is the total with amendments, or the initial value when no total is published (old EOJN report).

    /** {@code [year, contracts, value]} per signing year. */
    @Query("""
            select year(c.dateSigned), count(c), sum(coalesce(c.totalValue, c.initialValue)) from Contract c
            left join c.supplier s
            where c.buyer.id = :buyerId and c.dateSigned is not null
              and (s.id is null or s.orgType <> com.datathon.api.organization.OrganizationType.NATURAL_PERSON)
            group by year(c.dateSigned) order by year(c.dateSigned)""")
    List<Object[]> sumByYearForBuyer(@Param("buyerId") Long buyerId);

    /** {@code [cpv2, contracts, value]} per CPV division, largest value first. */
    @Query("""
            select substring(c.cpv, 1, 2), count(c), sum(coalesce(c.totalValue, c.initialValue)) from Contract c
            left join c.supplier s
            where c.buyer.id = :buyerId and c.cpv is not null
              and (s.id is null or s.orgType <> com.datathon.api.organization.OrganizationType.NATURAL_PERSON)
            group by substring(c.cpv, 1, 2) order by sum(coalesce(c.totalValue, c.initialValue)) desc nulls last""")
    List<Object[]> sumByCpvForBuyer(@Param("buyerId") Long buyerId);

    /** {@code [supplier, contracts, value]} without crafts and natural persons (lists with names), largest first. */
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

    /** {@code [dataset title, landing url, format class, files, contracts]}: where the buyer's contracts come from. */
    @Query("""
            select d.title, d.landingUrl, f.formatClass, count(distinct f.id), count(c) from Contract c
            join c.provenance p join p.rawRecord r join r.file f join f.dataset d
            left join c.supplier s
            where c.buyer.id = :buyerId
              and (s.id is null or s.orgType <> com.datathon.api.organization.OrganizationType.NATURAL_PERSON)
            group by d.title, d.landingUrl, f.formatClass order by count(c) desc""")
    List<Object[]> sourcesForBuyer(@Param("buyerId") Long buyerId);
}
