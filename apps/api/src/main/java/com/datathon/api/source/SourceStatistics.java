package com.datathon.api.source;

import com.datathon.api.common.persistence.DatabaseCode;
import jakarta.persistence.EntityManager;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Counts for the sources page. They span several entities (files, raw records, contracts, ingest issues), so they
 * are plain JPQL here rather than spread over repositories of other features. Real data only.
 */
@Component
@RequiredArgsConstructor
class SourceStatistics {

    private final EntityManager entityManager;

    Map<Long, Long> rowsPerFile(Collection<Long> fileIds) {
        return countsById("select r.file.id, count(r) from RawRecord r where r.file.id in :ids group by r.file.id", fileIds);
    }

    Map<Long, Long> contractsPerFile(Collection<Long> fileIds) {
        return countsById("""
                select r.file.id, count(c) from Contract c join c.provenance p join p.rawRecord r
                where r.file.id in :ids group by r.file.id""", fileIds);
    }

    long realRows() {
        return single("select count(r) from RawRecord r where r.file.dataset.isSynthetic = false");
    }

    /** Contracts with natural persons stay in the database but are never exposed (GDPR). */
    long hiddenContracts() {
        return single("""
                select count(c) from Contract c join c.supplier s
                where c.isSynthetic = false and s.orgType = com.datathon.api.organization.OrganizationType.NATURAL_PERSON""");
    }

    Map<String, Long> buyersByType() {
        return countsByKey("""
                select b.orgType, count(distinct b.id) from Contract c join c.buyer b
                where c.isSynthetic = false group by b.orgType""");
    }

    List<Object[]> contractsByYear() {
        return entityManager.createQuery("""
                select year(c.dateSigned), count(c) from Contract c
                where c.isSynthetic = false and c.dateSigned is not null
                group by year(c.dateSigned) order by year(c.dateSigned)""", Object[].class).getResultList();
    }

    Map<String, Long> issuesByCode() {
        return countsByKey("""
                select i.code, count(i) from IngestIssue i, SourceFile f
                where f.id = i.fileId and f.dataset.isSynthetic = false
                group by i.code order by count(i) desc""");
    }

    private Map<Long, Long> countsById(String jpql, Collection<Long> ids) {
        Map<Long, Long> counts = new LinkedHashMap<>();
        if (ids.isEmpty()) {
            return counts;
        }
        for (Object[] row : entityManager.createQuery(jpql, Object[].class).setParameter("ids", ids).getResultList()) {
            counts.put((Long) row[0], (Long) row[1]);
        }
        return counts;
    }

    /** Keys are database codes ({@code jls}) or plain strings. */
    private Map<String, Long> countsByKey(String jpql) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (Object[] row : entityManager.createQuery(jpql, Object[].class).getResultList()) {
            Object key = row[0];
            String name = key instanceof DatabaseCode code ? code.code() : String.valueOf(key);
            counts.put(name, (Long) row[1]);
        }
        return counts;
    }

    private long single(String jpql) {
        return entityManager.createQuery(jpql, Long.class).getSingleResult();
    }
}
