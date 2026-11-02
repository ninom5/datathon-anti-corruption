package com.datathon.api.contract;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ContractRepository extends JpaRepository<Contract, Long> {

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

    long countByIsSyntheticFalse();
}
