package com.datathon.api.source;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SourceFileRepository extends JpaRepository<SourceFile, Long> {

    Optional<SourceFile> findFirstByUrlAndSha256(String url, String sha256);

    List<SourceFile> findByDatasetIsSyntheticFalse();

    @Query("select max(f.retrievedAt) from SourceFile f where f.dataset.isSynthetic = false")
    Instant findLatestRealRetrieval();

    @Query("select f from SourceFile f where f.dataset.id in :datasetIds order by f.dataset.id, f.id")
    List<SourceFile> findByDatasetIds(@Param("datasetIds") Collection<Long> datasetIds);
}
