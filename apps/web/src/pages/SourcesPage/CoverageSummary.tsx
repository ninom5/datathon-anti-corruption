import type {FileStatus, SourcesSummary} from "@/types";
import {buyerTypeLabels, fileStatusHints, fileStatusLabels, formatDateTime, formatNumber, issueCodeLabel} from "@/utils";
import styles from "./SourcesPage.module.css";

interface CoverageSummaryProps {
    summary: SourcesSummary;
}

const STATUS_ORDER: FileStatus[] = ["uvezeno", "format_nije_podrzan", "nestrukturirano", "necitljivo", "nedostupno"];

export const CoverageSummary = ({summary}: CoverageSummaryProps) => (
    <section className={styles.coverage} aria-labelledby="coverage-title">
        <h2 id="coverage-title">Pokrivenost</h2>
        <p className={styles.lead}>
            {formatNumber(summary.datasets)} skupova podataka i {formatNumber(summary.files)} datoteka s data.gov.hr,
            zadnji dohvat {formatDateTime(summary.latestRetrievedAt)}. Iz {formatNumber(summary.rows)} pročitanih redaka
            nastalo je {formatNumber(summary.contracts)} jedinstvenih ugovora.
        </p>
        <div className={styles.coverageGrid}>
            <div>
                <h3>Datoteke po statusu</h3>
                <table className={styles.compactTable}>
                    <tbody>
                    {STATUS_ORDER.filter((status) => summary.filesByStatus[status]).map((status) => (
                        <tr key={status}>
                            <th scope="row">
                                {fileStatusLabels[status]}
                                <span className={styles.hint}>{fileStatusHints[status]}</span>
                            </th>
                            <td className="tabular">{formatNumber(summary.filesByStatus[status] ?? 0)}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
            <div>
                <h3>Naručitelji s ugovorima</h3>
                <table className={styles.compactTable}>
                    <tbody>
                    {Object.entries(summary.buyersByType).map(([type, count]) => (
                        <tr key={type}>
                            <th scope="row">{buyerTypeLabels[type] ?? type}</th>
                            <td className="tabular">{formatNumber(count)}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
                <h3>Ugovori po godini sklapanja</h3>
                <table className={styles.compactTable}>
                    <tbody>
                    {summary.contractsByYear.map((row) => (
                        <tr key={row.year}>
                            <th scope="row">{row.year}.{row.incomplete && <span className={styles.hint}>nepotpuna godina</span>}</th>
                            <td className="tabular">{formatNumber(row.contracts)}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
            <div>
                <h3>Uvoz i zaštita podataka</h3>
                <table className={styles.compactTable}>
                    <tbody>
                    {Object.entries(summary.issuesByCode).map(([code, count]) => (
                        <tr key={code}>
                            <th scope="row">{issueCodeLabel(code)}</th>
                            <td className="tabular">{formatNumber(count)}</td>
                        </tr>
                    ))}
                    <tr>
                        <th scope="row">
                            Ugovori s fizičkim osobama
                            <span className={styles.hint}>Ne prikazuju se (zaštita osobnih podataka).</span>
                        </th>
                        <td className="tabular">{formatNumber(summary.hiddenContracts)}</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </section>
);
