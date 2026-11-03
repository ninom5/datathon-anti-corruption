import {useState} from "react";
import {Button, IndicatorCard} from "@/components";
import type {BuyerProfile, Indicator} from "@/types";
import {formatNumber, ruleOptions} from "@/utils";
import styles from "@/components/DataView/DataView.module.css";

interface IndicatorGroupProps { indicators: Indicator[]; label: string; short: string }

const IndicatorGroup = ({indicators, label, short}: IndicatorGroupProps) => {
    const [isExpanded, setExpanded] = useState(false);
    if (!indicators.length) return null;
    const visible = isExpanded ? indicators : indicators.slice(0, 10);
    return (
        <section id={`rule-${short}`} className={styles.ruleGroup}>
            <h3>{short} · {label} ({formatNumber(indicators.length)})</h3>
            {visible.map((indicator) => <IndicatorCard key={indicator.id} indicator={indicator} compact/>)}
            {indicators.length > 10 && <Button variant="secondary" aria-expanded={isExpanded} onClick={() => setExpanded(!isExpanded)}>{isExpanded ? "Prikaži prvih 10" : `Prikaži sve (${formatNumber(indicators.length)})`}</Button>}
        </section>
    );
};

interface BuyerIndicatorsProps { profile: BuyerProfile }

export const BuyerIndicators = ({profile}: BuyerIndicatorsProps) => (
    <section className={styles.plainSection}>
        <h2>Indikatori u objavljenim ugovorima</h2>
        <p className={styles.note}>Tri odvojene dimenzije opisuju svaki indikator. Broj indikatora nije ocjena naručitelja; jedan ugovor može pripadati više indikatora. Detalj vodi do dokaza i redaka izvora.</p>
        <ul className={styles.ruleCounts}>{ruleOptions.map((rule) => {
            const count = profile.indicatorCounts.find((item) => item.ruleCode === rule.code);
            return <li key={rule.code}>{count?.total ? <a href={`#rule-${rule.short}`}>{rule.short} · {rule.label}: <strong>{formatNumber(count.total)}</strong></a> : <span>{rule.short} · {rule.label}: 0</span>}{!!count?.informative && <span> ({count.informative} informativno)</span>}</li>;
        })}</ul>
        {profile.indicators.length === 0 && <p>Pravila nisu pronašla indikatore u dostupnim ugovorima. To ne znači da su ugovori provjereni.</p>}
        {ruleOptions.map((rule) => <IndicatorGroup key={rule.code} short={rule.short} label={rule.label} indicators={profile.indicators.filter((indicator) => indicator.ruleCode === rule.code)}/>) }
    </section>
);
