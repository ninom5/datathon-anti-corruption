import type {ContractSummary} from "@/types";
import {formatEurMillions, formatNumber, plural} from "@/utils";
import styles from "./ContractsPage.module.css";

interface SummaryBarProps {
    summary: ContractSummary;
    isSyntheticShown: boolean;
}

/**
 * Totals over real data only. Crafts are left out of summaries (GDPR decision 2026-11-02), so the line says how
 * many were left out and the numbers can be reconciled with the table.
 */
export const SummaryBar = ({summary, isSyntheticShown}: SummaryBarProps) => (
    <section className={styles.summary} aria-label="Sažetak odabira">
        <p className={styles.summaryLine}>
            <strong className="tabular">{formatNumber(summary.contracts)}</strong>{" "}
            {plural(summary.contracts, "ugovor", "ugovora", "ugovora")}
            <span className={styles.dot}>·</span>
            <strong className="tabular">{formatEurMillions(summary.totalValue)}</strong> bez PDV-a
            <span className={styles.dot}>·</span>
            <strong className={`tabular ${styles.signalText}`}>{formatNumber(summary.worthChecking)}</strong> vrijedi provjeriti
            <span className={styles.dot}>·</span>
            <strong className="tabular">{formatNumber(summary.possibleEntryErrors)}</strong>{" "}
            {plural(summary.possibleEntryErrors, "moguća greška", "moguće greške", "mogućih grešaka")} u unosu
        </p>
        <p className={styles.summaryNote}>
            Sažetak obuhvaća samo stvarne podatke
            {summary.craftContracts > 0 && (
                <>, uključujući {formatNumber(summary.craftContracts)}{" "}
                    {plural(summary.craftContracts, "ugovor", "ugovora", "ugovora")} s obrtima (nazivi obrta su skriveni
                    radi zaštite osobnih podataka)</>
            )}
            .
            {summary.incompleteYears.length > 0 && <> {summary.incompleteYears.join(", ")}. je nepotpuna godina.</>}
            {isSyntheticShown && summary.syntheticContracts > 0 && (
                <> Tablica uključuje i {formatNumber(summary.syntheticContracts)} sintetičkih ugovora koji nisu u sažetku.</>
            )}
        </p>
    </section>
);
