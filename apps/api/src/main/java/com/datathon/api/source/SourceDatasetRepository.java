package com.datathon.api.source;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceDatasetRepository extends JpaRepository<SourceDataset, Long> {

    Optional<SourceDataset> findByPortalAndExternalId(String portal, String externalId);
}
