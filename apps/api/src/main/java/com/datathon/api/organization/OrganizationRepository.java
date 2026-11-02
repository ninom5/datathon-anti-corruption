package com.datathon.api.organization;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    Optional<Organization> findByOib(String oib);

    Optional<Organization> findFirstByOibIsNullAndNameNormAndIsSynthetic(String nameNorm, boolean isSynthetic);
}
