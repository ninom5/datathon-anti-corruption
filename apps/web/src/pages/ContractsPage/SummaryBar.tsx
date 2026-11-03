import type {ContractSummary} from "@/types";
import {formatEurMillions, formatNumber, plural} from "@/utils";
import styles from "./ContractsPage.module.css";

interface SummaryBarProps {
    summary: ContractSummary;
    isSyntheticShown: boolean;
}

/** Totals over real data, including crafts; only their names are hidden. */
export const SummaryBar = ({summary, isSyntheticShown}: SummaryBarProps) => (
    <section className={styles.summary} aria-label="Sažetak odabira">
        <p className={styles.summaryLine}>
            <strong className="tabular-nums">{formatNumber(summary.contracts)}</strong>{" "}
            {plural(summary.contracts, "ugovor", "ugovora", "ugovora")}
            <span className={styles.dot}>·</span>
            <strong className="tabular-nums">{formatEurMillions(summary.totalValue)}</strong> bez PDV-a
            <span className={styles.dot}>·</span>
            <strong className={`tabular-nums ${styles.signalText}`}>{formatNumber(summary.worthChecking)}</strong> vrijedi provjeriti
            <span className={styles.dot}>·</span>
            <strong className="tabular-nums">{formatNumber(summary.possibleEntryErrors)}</strong>{" "}
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
