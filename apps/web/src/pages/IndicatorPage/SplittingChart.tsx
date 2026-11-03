import {Link} from "wouter";
import {ChartFrame} from "@/components";
import {contractPath} from "@/routes";
import type {IndicatorDetail} from "@/types";
import {formatDate, formatEur, formatEurRounded} from "@/utils";
import {buildTimeline} from "@/pages/IndicatorPage";
import styles from "@/components/DataView.module.css";

interface SplittingChartProps { indicator: IndicatorDetail }

export const SplittingChart = ({indicator}: SplittingChartProps) => {
    const points = buildTimeline(indicator.contracts);
    const threshold = indicator.subject?.threshold;
    if (!points?.length) return <p>Vremenska os nije dostupna bez datuma i početnog iznosa svakog ugovora. Dostupni podaci su u tablici ugovora.</p>;
    const hasThreshold = threshold !== undefined && threshold > 0;
    const total = points[points.length - 1].cumulative;
    const maximum = Math.max(1, total, threshold ?? 0, ...points.map((point) => point.amount)) * 1.1;
    const first = points[0].timestamp;
    const last = points[points.length - 1].timestamp;
    const day = 86_400_000;
    const start = first === last ? first - day : first;
    const end = first === last ? last + day : last;
    const x = (date: number) => 110 + (date - start) / (end - start) * 700;
    const y = (amount: number) => 280 - amount / maximum * 230;
    let step = `M ${x(start)} ${y(0)}`;
    for (const point of points) step += ` H ${x(point.timestamp)} V ${y(point.cumulative)}`;
    step += ` H ${x(end)}`;
    return (
        <ChartFrame title="Ugovori u vremenskom prozoru" description={<>Početni iznosi iz ugovora, EUR bez PDV-a. Zbroj prozora: <strong>{formatEur(total)}</strong>. {hasThreshold ? `Prag koji koristi pravilo: ${formatEur(threshold)} (najniži u prozoru).` : "Prag nije objavljen; crta praga se ne prikazuje."} Izvori su u bloku Dokazi i u detaljima ugovora.</>}
            table={<table className={styles.table}><caption>Početni iznosi, kumulativni zbroj i prag pravila F</caption>
                <thead><tr><th scope="col">Datum / ugovor</th><th scope="col" className={styles.numeric}>Početni iznos</th><th scope="col" className={styles.numeric}>Kumulativni zbroj</th><th scope="col" className={styles.numeric}>Prag</th></tr></thead>
                <tbody>{points.map((point) => <tr key={point.contract.id}><td><Link href={contractPath(point.contract.id)}>{formatDate(point.contract.dateSigned)} · {point.contract.subject}</Link></td><td className={styles.numeric}>{formatEur(point.amount)}</td><td className={styles.numeric}>{formatEur(point.cumulative)}</td><td className={styles.numeric}>{formatEur(threshold)}</td></tr>)}</tbody>
            </table>}>
            {indicator.subject?.cpvLevel === 2 && <p className={styles.context}>Podudara se samo CPV odjeljak (prve dvije znamenke), uz različite CPV podskupine. Zato je pouzdanost dokaza slaba; predmeti ugovora mogu biti različiti.</p>}
            <div className={styles.legend} aria-label="Legenda"><span><svg aria-hidden="true"><circle cx="15" cy="7" r="4" className={styles.point}/></svg>Početni iznos ugovora</span><span><svg aria-hidden="true"><path d="M0 12 H15 V2 H30" className={styles.cumulative}/></svg>Kumulativni zbroj</span>{hasThreshold && <span><svg aria-hidden="true"><path d="M0 7 H30" className={styles.threshold}/></svg>Prag pravila</span>}</div>
            <div className={styles.plotWrap} tabIndex={0} role="region" aria-label="Vremenska os; vodoravno pomicanje na manjim ekranima">
                <svg className={styles.plot} viewBox="0 0 900 330" role="img" aria-label={`${points.length} ugovora od ${formatDate(points[0].contract.dateSigned)} do ${formatDate(points[points.length - 1].contract.dateSigned)}. Zbroj ${formatEur(total)}; prag ${formatEur(threshold)}. Točke su pojedinačni iznosi, stepenasta linija je zbroj.`}>
                    {[0, .25, .5, .75, 1].map((fraction) => <g key={fraction}><line x1="110" x2="830" y1={y(maximum * fraction)} y2={y(maximum * fraction)} className={styles.gridLine}/><text x="100" y={y(maximum * fraction) + 4} textAnchor="end">{formatEurRounded(maximum * fraction)}</text></g>)}
                    <path d={step} className={styles.cumulative}><title>Kumulativni zbroj: {formatEur(total)}</title></path>
                    {hasThreshold && <line x1="110" x2="830" y1={y(threshold)} y2={y(threshold)} className={styles.threshold}><title>Prag pravila: {formatEur(threshold)}</title></line>}
                    {points.map((point) => <circle key={point.contract.id} cx={x(point.timestamp)} cy={y(point.amount)} r="5" className={styles.point}><title>{formatDate(point.contract.dateSigned)} · ugovor {point.contract.id} · {point.contract.subject}: {formatEur(point.amount)}</title></circle>)}
                    {[0, .5, 1].map((fraction) => <text key={fraction} x={110 + 700 * fraction} y="307" textAnchor="middle">{formatDate(new Date(start + (end - start) * fraction).toISOString())}</text>)}
                </svg>
            </div>
            <p className={styles.note}>Ugovori istog dana na vremenskoj osi dijele datum; točke s istim iznosom mogu se preklapati. Tablica prikazuje svaki ugovor zasebno.</p>
        </ChartFrame>
    );
};
