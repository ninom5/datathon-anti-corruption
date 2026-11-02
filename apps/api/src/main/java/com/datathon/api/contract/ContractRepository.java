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

    /** Everything a contract rule needs, in one query (contract, supplier and the source file of its row). */
    @Query("""
            select c from Contract c
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

    long countByIsSyntheticFalse();
}
