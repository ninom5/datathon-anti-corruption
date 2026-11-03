import type {ContractValues} from "@/types";
import {formatEur} from "@/utils";
import styles from "./ContractPage.module.css";

interface ValuesTableProps {
    values: ContractValues;
}

export const ValuesTable = ({values}: ValuesTableProps) => {
    const rows: {label: string; net: number | null; gross: number | null}[] = [
        {label: "Početni iznos", net: values.initialValue, gross: values.initialValueGross},
        {label: "Izmjene", net: values.amendmentsValue, gross: null},
        {label: "Ukupno (s izmjenama)", net: values.totalValue, gross: values.totalValueGross},
    ];
    return (
        <section className={styles.block} aria-labelledby="values-title">
            <h2 id="values-title">Iznosi</h2>
            <table className={styles.valuesTable}>
                <thead>
                <tr>
                    <th scope="col"><span className="visually-hidden">Stavka</span></th>
                    <th scope="col">bez PDV-a</th>
                    <th scope="col">s PDV-om</th>
                </tr>
                </thead>
                <tbody>
                {rows.map((row) => (
                    <tr key={row.label}>
                        <th scope="row">{row.label}</th>
                        <td className="tabular-nums">{formatEur(row.net)}</td>
                        <td className="tabular-nums">{row.label === "Izmjene" ? "–" : formatEur(row.gross)}</td>
                    </tr>
                ))}
                <tr>
                    <th scope="row">Isplaćeno</th>
                    <td className={styles.muted}>–</td>
                    <td className="tabular-nums">{formatEur(values.paidAmount)}</td>
                </tr>
                </tbody>
            </table>
            {values.convertedFromHrk && <p className={styles.muted}>Iznosi su preračunati iz kuna (7,53450 kn = 1 €).</p>}
        </section>
    );
};
