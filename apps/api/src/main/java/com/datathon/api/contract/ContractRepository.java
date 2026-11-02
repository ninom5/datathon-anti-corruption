package com.datathon.api.contract;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractRepository extends JpaRepository<Contract, Long> {

    Optional<Contract> findByNaturalKey(String naturalKey);
}
