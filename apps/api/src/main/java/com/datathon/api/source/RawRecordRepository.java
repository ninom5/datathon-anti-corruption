package com.datathon.api.source;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RawRecordRepository extends JpaRepository<RawRecord, Long> {

    boolean existsByFile(SourceFile file);

    Optional<RawRecord> findByFileAndLocator(SourceFile file, String locator);
}
