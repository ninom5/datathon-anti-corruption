package com.datathon.api.organization;

import com.datathon.api.common.ExternalSourceException;
import com.datathon.api.common.TooManyRequestsException;
import com.datathon.api.integration.sudreg.SudregClient;
import com.datathon.api.integration.sudreg.SudregCompany;
import com.datathon.api.integration.sudreg.SudregProperties;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * Looks up suppliers in the company register (Sudreg) in the background, one request per second, and stores a
 * snapshot per supplier. ~1.600 suppliers take about half an hour; progress via {@link #status()}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyRegisterService {

    static final int MAX_CONSECUTIVE_ERRORS = 5;

    private final SudregClient sudregClient;
    private final SudregProperties properties;
    private final CompanySnapshotRepository snapshotRepository;
    private final OrganizationRepository organizationRepository;
    private final TransactionTemplate transactionTemplate;
    private final JsonMapper jsonMapper;
    /** Daemon thread: an unfinished lookup must never keep the JVM (or a test run) from exiting. */
    private final ExecutorService worker = Executors.newSingleThreadExecutor(
            Thread.ofPlatform().name("sudreg-lookup").daemon().factory());
    private final AtomicReference<RunStatus> current = new AtomicReference<>(RunStatus.idle());

    /**
     * @param stopReason null while running or when every supplier was looked up
     */
    public record RunStatus(boolean running, int total, int done, int found, int notFound, int errors,
                            Instant startedAt, Instant finishedAt, String stopReason) {

        static RunStatus idle() {
            return new RunStatus(false, 0, 0, 0, 0, 0, null, null, null);
        }

        RunStatus next(boolean isFound, boolean isError) {
            return new RunStatus(true, total, done + 1, found + (isFound ? 1 : 0),
                    notFound + (!isFound && !isError ? 1 : 0), errors + (isError ? 1 : 0), startedAt, null, null);
        }

        RunStatus finished(String reason) {
            return new RunStatus(false, total, done, found, notFound, errors, startedAt, Instant.now(), reason);
        }
    }

    public RunStatus status() {
        return current.get();
    }

    /** @param limit at most this many suppliers; null = all without a snapshot */
    public RunStatus start(Integer limit) {
        if (!sudregClient.isConfigured()) {
            throw new ExternalSourceException("Sudreg", "SUDREG_CLIENT_ID i SUDREG_CLIENT_SECRET nisu postavljeni.");
        }
        if (current.get().running()) {
            throw new TooManyRequestsException("Dohvat iz Sudskog registra je već u tijeku.");
        }
        List<Object[]> suppliers = snapshotRepository.findSuppliersWithoutSnapshot();
        List<Object[]> selected = limit == null ? suppliers : suppliers.stream().limit(limit).toList();
        current.set(new RunStatus(true, selected.size(), 0, 0, 0, 0, Instant.now(), null, null));
        worker.submit(() -> lookUpAll(selected));
        return current.get();
    }

    private void lookUpAll(List<Object[]> suppliers) {
        int consecutiveErrors = 0;
        for (Object[] supplier : suppliers) {
            try {
                Thread.sleep(properties.minIntervalMillis());
                boolean isFound = lookUp(((Number) supplier[0]).longValue(), (String) supplier[1]);
                current.updateAndGet(status -> status.next(isFound, false));
                consecutiveErrors = 0;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                current.updateAndGet(status -> status.finished("prekinuto"));
                return;
            } catch (RuntimeException e) {
                log.warn("Sudreg lookup failed: {}", e.getMessage());
                current.updateAndGet(status -> status.next(false, true));
                if (++consecutiveErrors >= MAX_CONSECUTIVE_ERRORS) {
                    current.updateAndGet(status -> status.finished(MAX_CONSECUTIVE_ERRORS + " grešaka zaredom"));
                    return;
                }
            }
        }
        RunStatus done = current.updateAndGet(status -> status.finished(null));
        log.info("Sudreg: {} looked up, {} found, {} not in the register, {} errors", done.done(), done.found(),
                done.notFound(), done.errors());
    }

    private boolean lookUp(Long organizationId, String oib) {
        Optional<SudregCompany> company = sudregClient.findByOib(oib);
        transactionTemplate.executeWithoutResult(status -> snapshotRepository.save(snapshot(organizationId, company)));
        return company.isPresent();
    }

    private CompanySnapshot snapshot(Long organizationId, Optional<SudregCompany> found) {
        CompanySnapshot snapshot = new CompanySnapshot(organizationRepository.getReferenceById(organizationId),
                CompanySnapshot.SOURCE_SUDREG, Instant.now());
        if (found.isEmpty()) {
            snapshot.setNotFound(true);
            return snapshot;
        }
        SudregCompany company = found.get();
        snapshot.setMbs(company.mbs());
        snapshot.setFoundedOn(company.foundedOn());
        snapshot.setDissolvedOn(company.dissolvedOn());
        snapshot.setLegalForm(company.legalForm());
        snapshot.setMainNkd(company.nkdCode());
        snapshot.setMainNkdName(company.nkdName());
        snapshot.setNkdVersion(company.nkdVersion());
        snapshot.setInBankruptcy(company.inBankruptcy());
        snapshot.setInLiquidation(company.inLiquidation());
        snapshot.setStatusText(company.statusText());
        snapshot.setPayload(jsonMapper.writeValueAsString(company.reducedPayload()));
        return snapshot;
    }
}
