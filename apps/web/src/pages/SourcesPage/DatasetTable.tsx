import type {SourceDataset} from "@/types";
import {fileStatusLabels, formatDateTime, formatNumber} from "@/utils";
import styles from "./SourcesPage.module.css";

interface DatasetTableProps {
    datasets: SourceDataset[];
}

export const DatasetTable = ({datasets}: DatasetTableProps) => (
    <div className={styles.tableWrap}>
        <table className={styles.table}>
            <caption className="visually-hidden">Skupovi podataka i datoteke</caption>
            <thead>
            <tr>
                <th scope="col">Izdavatelj i skup</th>
                <th scope="col">Datoteka</th>
                <th scope="col">Status</th>
                <th scope="col" className={styles.numeric}>Redaka</th>
                <th scope="col" className={styles.numeric}>Ugovora</th>
                <th scope="col">Dohvaćeno</th>
            </tr>
            </thead>
            {datasets.map((dataset) => (
                <tbody key={dataset.id} className={styles.datasetGroup}>
                {dataset.files.length === 0 && (
                    <tr>
                        <th scope="row" className={styles.datasetCell}>
                            <span className={styles.publisher}>{dataset.publisher ?? "izdavatelj nije naveden"}</span>
                            <span>{dataset.title}</span>
                        </th>
                        <td colSpan={5} className={styles.hint}>Skup nema datoteka.</td>
                    </tr>
                )}
                {dataset.files.map((file, index) => (
                    <tr key={file.id}>
                        {index === 0 && (
                            <th scope="rowgroup" rowSpan={dataset.files.length} className={styles.datasetCell}>
                                <span className={styles.publisher}>{dataset.publisher ?? "izdavatelj nije naveden"}</span>
                                {dataset.landingUrl
                                    ? <a href={dataset.landingUrl} target="_blank" rel="noreferrer">{dataset.title}</a>
                                    : <span>{dataset.title}</span>}
                                {dataset.licenseId && <span className={styles.hint}>licenca: {dataset.licenseId}</span>}
                            </th>
                        )}
                        <td data-label="Datoteka" className={styles.fileCell}>
                            <a href={file.url} target="_blank" rel="noreferrer">{file.name ?? file.url}</a>
                            {file.format && <span className={styles.hint}>{file.format}</span>}
                        </td>
                        <td data-label="Status">
                            <span className={file.status === "uvezeno" ? styles.statusOk : styles.status}>
                                {fileStatusLabels[file.status]}
                            </span>
                        </td>
                        <td data-label="Redaka" className={`${styles.numeric} tabular`}>{formatNumber(file.rows)}</td>
                        <td data-label="Ugovora" className={`${styles.numeric} tabular`}>{formatNumber(file.contracts)}</td>
                        <td data-label="Dohvaćeno" className="tabular">{formatDateTime(file.retrievedAt)}</td>
                    </tr>
                ))}
                </tbody>
            ))}
        </table>
    </div>
);
