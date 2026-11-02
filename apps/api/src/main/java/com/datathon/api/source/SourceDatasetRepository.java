package com.datathon.api.source;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceDatasetRepository extends JpaRepository<SourceDataset, Long> {

    Optional<SourceDataset> findByPortalAndExternalId(String portal, String externalId);

    Page<SourceDataset> findByIsSyntheticFalse(Pageable pageable);

    long countByIsSyntheticFalse();
}
