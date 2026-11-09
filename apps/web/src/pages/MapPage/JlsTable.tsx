import {TextInput} from "@/components";
import {useState} from "react";
import type {MunicipalityOverview} from "@/types";
import {formatNumber} from "@/utils";
import styles from "./MapPage.module.css";

const per100Format = new Intl.NumberFormat("hr-HR", {maximumFractionDigits: 1});

const matches = (municipality: MunicipalityOverview, query: string) =>
    query === "" || `${municipality.name} ${municipality.county}`.toLocaleLowerCase("hr").includes(query);

interface JlsTableProps {
    municipalities: MunicipalityOverview[];
    onSelect: (oib: string) => void;
}

export const JlsTable = ({municipalities, onSelect}: JlsTableProps) => {
    const [query, setQuery] = useState("");
    const [isWithDataOnly, setIsWithDataOnly] = useState(true);
    const normalizedQuery = query.trim().toLocaleLowerCase("hr");
    const rows = municipalities
        .filter((municipality) => !isWithDataOnly || municipality.contracts > 0)
        .filter((municipality) => matches(municipality, normalizedQuery));

    return (
        <div className={styles.tableBlock}>
            <div className={styles.tableControls}>
                <label className={styles.searchLabel}>
                    Pretraži JLS ili županiju
                    <TextInput type="search" name="jlsQuery" autoComplete="off" placeholder="npr. Split" value={query}
                               onChange={(event) => setQuery(event.target.value)} className={styles.searchInput}/>
                </label>
                <label className={styles.toggle}>
                    <input type="checkbox" checked={isWithDataOnly} onChange={(event) => setIsWithDataOnly(event.target.checked)}/>
                    Samo JLS s registrom ugovora
                </label>
            </div>
            <div className={styles.tableScroll}>
                <table className={styles.table}>
                    <caption className={styles.visuallyHidden}>JLS s brojem ugovora i indikatora</caption>
                    <thead>
                    <tr>
                        <th scope="col">JLS</th>
                        <th scope="col">Županija</th>
                        <th scope="col" className={styles.number}>Stanovnika</th>
                        <th scope="col" className={styles.number}>Ugovora</th>
                        <th scope="col" className={styles.number}>Vrijedi provjeriti</th>
                        <th scope="col" className={styles.number}>Na 100 ugovora</th>
                    </tr>
                    </thead>
                    <tbody>
                    {rows.map((municipality) => (
                        <tr key={municipality.oib}>
                            <th scope="row">
                                <button type="button" className={styles.linkButton} onClick={() => onSelect(municipality.oib)}>
                                    {municipality.name}
                                </button>
                            </th>
                            <td>{municipality.county}</td>
                            <td className={styles.number}>
                                {municipality.population2021 === null ? "–" : formatNumber(municipality.population2021)}
                            </td>
                            {municipality.contracts === 0 ? (
                                <td colSpan={3} className={styles.muted}>bez strojno čitljivog registra</td>
                            ) : (
                                <>
                                    <td className={styles.number}>{formatNumber(municipality.contracts)}</td>
                                    <td className={styles.number}>{formatNumber(municipality.worthChecking)}</td>
                                    <td className={styles.number}>
                                        {municipality.worthCheckingPer100 === null ? "–" : per100Format.format(municipality.worthCheckingPer100)}
                                    </td>
                                </>
                            )}
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
            {rows.length === 0 && <p className={styles.muted}>Nijedna JLS ne odgovara pretrazi.</p>}
        </div>
    );
};
