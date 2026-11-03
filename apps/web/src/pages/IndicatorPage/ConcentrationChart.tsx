import {ChartFrame, HorizontalBars, OrganizationName} from "@/components";
import type {IndicatorDetail} from "@/types";
import {cpvDivisionLabel, formatEur, formatNumber} from "@/utils";
import {buildSupplierShares, supplierChartLabel} from "@/pages/IndicatorPage";
import styles from "@/components/DataView.module.css";

interface ConcentrationChartProps { indicator: IndicatorDetail }
const percent = (value: number) => new Intl.NumberFormat("hr-HR", {style: "percent", maximumFractionDigits: 1}).format(value);

export const ConcentrationChart = ({indicator}: ConcentrationChartProps) => {
    const shares = buildSupplierShares(indicator.contracts);
    if (!shares?.length) return <p>Udjeli nisu dostupni jer iznosi nedostaju ili zbroj nije pozitivan. Dostupni podaci su u tablici ugovora.</p>;
    const total = shares.reduce((sum, supplier) => sum + supplier.value, 0);
    return (
        <ChartFrame title="Udio dobavljača u vrijednosti" description={<>{cpvDivisionLabel(indicator.subject?.cpv2)} · godine: {indicator.subject?.years?.join(", ") ?? "nisu objavljene"}. Ukupno {formatEur(total)} bez PDV-a. Koristi se ukupni iznos sa izmjenama, a ako nije objavljen, početni iznos. Izvori su u bloku Dokazi i detaljima ugovora.</>}
            table={<table className={styles.table}><caption>Udjeli u odabranom CPV odjeljku i razdoblju</caption>
                <thead><tr><th scope="col">Dobavljač</th><th scope="col" className={styles.numeric}>Ugovori</th><th scope="col" className={styles.numeric}>EUR bez PDV-a</th><th scope="col" className={styles.numeric}>Udio</th></tr></thead>
                <tbody>{shares.map((row) => <tr key={row.id ?? "unknown"}><td><OrganizationName organization={row.supplier} showOib/></td><td className={styles.numeric}>{formatNumber(row.contracts)}</td><td className={styles.numeric}>{formatEur(row.value)}</td><td className={styles.numeric}>{percent(row.share)}</td></tr>)}</tbody>
            </table>}>
            <HorizontalBars maximum={1} summary={`${shares.length} dobavljača. Najveći udio: ${supplierChartLabel(shares[0].supplier)}, ${percent(shares[0].share)}. Svi stupci na ljestvici od 0 do 100 %.`}
                rows={shares.map((row) => ({id: String(row.id), label: supplierChartLabel(row.supplier), value: row.share, formatted: percent(row.share)}))}/>
            <p className={styles.note}>Ljestvica je od 0 do 100 %. Obrti su uključeni u izračun, a njihovi nazivi su skriveni.</p>
        </ChartFrame>
    );
};
