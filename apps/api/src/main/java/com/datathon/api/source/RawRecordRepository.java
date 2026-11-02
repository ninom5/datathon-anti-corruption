package com.datathon.api.source;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RawRecordRepository extends JpaRepository<RawRecord, Long> {

    boolean existsByFile(SourceFile file);
}
