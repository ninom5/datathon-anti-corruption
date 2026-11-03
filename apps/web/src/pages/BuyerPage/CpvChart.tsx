import {ChartFrame, HorizontalBars} from "@/components";
import type {BuyerCpvTotal} from "@/types";
import {cpvDivisionLabel, formatEur, formatEurRounded, formatNumber} from "@/utils";
import styles from "@/components/DataView/DataView.module.css";

interface CpvChartProps { rows: BuyerCpvTotal[]; population: number | null | undefined }

export const CpvChart = ({rows, population}: CpvChartProps) => {
    const hasPopulation = population != null && population > 0;
    return (
        <ChartFrame title="Vrijednost ugovora po CPV odjeljku" description={<><a href="#buyer-sources">Izvor: registri ugovora</a>. Cijelo dostupno razdoblje, EUR bez PDV-a. Ugovori bez CPV oznake nisu u ovom prikazu. {hasPopulation ? "Iznosi po stanovniku u tablici koriste DZS 2021.; nisu godišnji prosjek." : "Prikaz po stanovniku nije dostupan bez podatka o stanovništvu."}</>}
            table={<table className={styles.table}><caption>Vrijednost po odjeljku za cijelo dostupno razdoblje</caption>
                <thead><tr><th scope="col">CPV odjeljak</th><th scope="col" className={styles.numeric}>Ugovori</th><th scope="col" className={styles.numeric}>EUR bez PDV-a</th>{hasPopulation && <th scope="col" className={styles.numeric}>EUR / stanovnik</th>}</tr></thead>
                <tbody>{rows.map((row) => <tr key={row.cpv2}><th scope="row">{cpvDivisionLabel(row.cpv2)}</th><td className={styles.numeric}>{formatNumber(row.contracts)}</td><td className={styles.numeric}>{formatEur(row.totalValue)}</td>{hasPopulation && <td className={styles.numeric}>{formatEur(row.totalValue / population)}</td>}</tr>)}</tbody>
            </table>}>
            {rows.length === 0 ? <p>Nema podataka po CPV odjeljku.</p> : <HorizontalBars summary={`Vrijednost ugovora u ${rows.length} CPV odjeljaka. Najveći: ${cpvDivisionLabel(rows[0].cpv2)}, ${formatEurRounded(rows[0].totalValue)}.`}
                rows={rows.map((row) => ({id: row.cpv2, label: cpvDivisionLabel(row.cpv2), value: row.totalValue, formatted: formatEurRounded(row.totalValue)}))}/>}
        </ChartFrame>
    );
};
