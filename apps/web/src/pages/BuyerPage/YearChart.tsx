import {useId} from "react";
import {ChartFrame} from "@/components";
import type {BuyerYearTotal} from "@/types";
import {formatEur, formatNumber} from "@/utils";
import styles from "@/components/DataView/DataView.module.css";

interface YearChartProps { years: BuyerYearTotal[] }

export const YearChart = ({years}: YearChartProps) => {
    const pattern = useId().replaceAll(":", "");
    const maximum = Math.max(10, Math.ceil(Math.max(0, ...years.map((row) => row.contracts)) / 10) * 10);
    const slot = 760 / Math.max(1, years.length);
    const hasIncomplete = years.some((row) => row.incomplete);
    return (
        <ChartFrame title="Ugovori po godini" description={<><a href="#buyer-sources">Izvor: registri ugovora</a>. Broj ugovora prema godini sklapanja.{hasIncomplete && " * Šrafura i zvjezdica označavaju nepotpunu godinu."}</>}
            table={<table className={styles.table}><caption>Broj i vrijednost ugovora po godini</caption>
                <thead><tr><th scope="col">Godina</th><th scope="col" className={styles.numeric}>Ugovori</th><th scope="col" className={styles.numeric}>Vrijednost bez PDV-a</th><th scope="col">Obuhvat</th></tr></thead>
                <tbody>{years.map((row) => <tr key={row.year}><th scope="row">{row.year}.</th><td className={styles.numeric}>{formatNumber(row.contracts)}</td><td className={styles.numeric}>{formatEur(row.totalValue)}</td><td>{row.incomplete ? "Nepotpuna godina *" : "Dostupni registri"}</td></tr>)}</tbody>
            </table>}>
            {years.length === 0 ? <p>Nema objavljenih podataka po godini.</p> : <div className={styles.plotWrap} tabIndex={0} role="region" aria-label="Graf ugovora po godini; vodoravno pomicanje na manjim ekranima">
                <svg className={styles.plot} viewBox="0 0 880 300" role="img" aria-label={`${years.reduce((total, row) => total + row.contracts, 0)} ugovora u ${years.length} godina. ${years.filter((row) => row.incomplete).map((row) => `${row.year}. nepotpuna`).join(", ")}. Točne brojke u tablici.`}>
                    <defs><pattern id={pattern} width="7" height="7" patternUnits="userSpaceOnUse" patternTransform="rotate(45)"><rect width="7" height="7" fill="var(--color-background)"/><line x1="0" y1="0" x2="0" y2="7" stroke="var(--color-signal)" strokeWidth="3"/></pattern></defs>
                    {[0, .5, 1].map((fraction) => <g key={fraction}><line x1="65" x2="840" y1={250 - fraction * 200} y2={250 - fraction * 200} className={styles.gridLine}/><text x="55" y={254 - fraction * 200} textAnchor="end">{formatNumber(Math.round(maximum * fraction))}</text></g>)}
                    {years.map((row, index) => {
                        const x = 65 + slot * index + slot / 2;
                        const height = row.contracts / maximum * 200;
                        return <g key={row.year}><title>{row.year}.: {row.contracts} ugovora, {formatEur(row.totalValue)}{row.incomplete ? ", nepotpuna godina" : ""}</title><rect x={x - slot * .3} y={250 - height} width={slot * .6} height={height} fill={row.incomplete ? `url(#${pattern})` : "var(--color-signal)"} stroke="var(--color-signal)"/><text x={x} y={240 - height} textAnchor="middle">{formatNumber(row.contracts)}</text><text x={x} y="277" textAnchor="middle">{row.year}{row.incomplete ? "*" : ""}</text></g>;
                    })}
                </svg>
            </div>}
        </ChartFrame>
    );
};
