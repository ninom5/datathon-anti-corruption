package com.datathon.api.ingest;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IngestIssueRepository extends JpaRepository<IngestIssue, Long> {
}
