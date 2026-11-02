package com.datathon.api.source;

import com.datathon.api.common.PageRequests;
import com.datathon.api.common.PageResponse;
import com.datathon.api.source.dto.SourceDatasetResponse;
import com.datathon.api.source.dto.SourceFileResponse;
import com.datathon.api.source.dto.SourcesSummary;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Sources page (docs/UI_UX.md §3.7): datasets, files, retrieval times and what was imported from each. */
@Service
@RequiredArgsConstructor
public class SourceService {

    private final SourceDatasetRepository datasetRepository;
    private final SourceFileRepository fileRepository;
    private final SourceStatistics statistics;
    private final DataCoverage dataCoverage;

    @Transactional(readOnly = true)
    public PageResponse<SourceDatasetResponse> list(boolean includeSynthetic, Integer page, Integer size) {
        var pageable = PageRequests.of(page, size, Sort.by("isSynthetic", "publisher", "title"));
        Page<SourceDataset> datasets = includeSynthetic
                ? datasetRepository.findAll(pageable)
                : datasetRepository.findByIsSyntheticFalse(pageable);
        Map<Long, List<SourceFileResponse>> files = filesByDataset(datasets.getContent().stream().map(SourceDataset::getId).toList());
        return PageResponse.from(datasets, dataset -> SourceDatasetResponse.from(dataset, files.getOrDefault(dataset.getId(), List.of())));
    }

    @Transactional(readOnly = true)
    public SourcesSummary summarize() {
        List<SourceFile> files = fileRepository.findByDatasetIsSyntheticFalse();
        Map<String, Long> filesByStatus = files.stream()
                .collect(Collectors.groupingBy(SourceFileResponse::statusOf, TreeMap::new, Collectors.counting()));
        int incompleteFromYear = dataCoverage.incompleteFromYear();
        List<SourcesSummary.YearCount> years = statistics.contractsByYear().stream()
                .map(row -> new SourcesSummary.YearCount((Integer) row[0], (Long) row[1], (Integer) row[0] >= incompleteFromYear))
                .toList();
        return new SourcesSummary(datasetRepository.countByIsSyntheticFalse(), files.size(), filesByStatus,
                statistics.realRows(), years.stream().mapToLong(SourcesSummary.YearCount::contracts).sum(),
                statistics.buyersByType(), years, incompleteFromYear, statistics.hiddenContracts(),
                statistics.issuesByCode(), fileRepository.findLatestRealRetrieval());
    }

    private Map<Long, List<SourceFileResponse>> filesByDataset(List<Long> datasetIds) {
        List<SourceFile> files = datasetIds.isEmpty() ? List.of() : fileRepository.findByDatasetIds(datasetIds);
        List<Long> fileIds = files.stream().map(SourceFile::getId).toList();
        Map<Long, Long> rows = statistics.rowsPerFile(fileIds);
        Map<Long, Long> contracts = statistics.contractsPerFile(fileIds);
        Map<Long, List<SourceFileResponse>> byDataset = new LinkedHashMap<>();
        for (SourceFile file : files) {
            byDataset.computeIfAbsent(file.getDataset().getId(), id -> new ArrayList<>())
                    .add(SourceFileResponse.from(file, rows.getOrDefault(file.getId(), 0L), contracts.getOrDefault(file.getId(), 0L)));
        }
        return byDataset;
    }
}
