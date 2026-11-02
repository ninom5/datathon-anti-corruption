package com.datathon.api.source;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceFileRepository extends JpaRepository<SourceFile, Long> {

    Optional<SourceFile> findFirstByUrlAndSha256(String url, String sha256);
}
