import type {Indicator} from "@/types";
import {
    dataQualityHints,
    dataQualityLabels,
    evidenceStrengthHints,
    evidenceStrengthLabels,
    formatDateTime,
    ruleLetter,
} from "@/utils";
import styles from "./ContractPage.module.css";

interface DimensionProps {
    title: string;
    value: string;
    hint: string;
}

/** One of the three equal boxes; there is never a combined score (docs/UI_UX.md §3.4). */
const Dimension = ({title, value, hint}: DimensionProps) => (
    <div className={styles.dimension}>
        <h4>{title}</h4>
        <p className={styles.dimensionValue}>{value}</p>
        <p className={styles.dimensionHint}>{hint}</p>
    </div>
);

interface IndicatorCardProps {
    indicator: Indicator;
}

export const IndicatorCard = ({indicator}: IndicatorCardProps) => {
    const isEntryError = indicator.possibleEntryError;
    return (
        <article className={`${styles.indicator} ${isEntryError ? styles.indicatorInfo : ""}`}>
            <header className={styles.indicatorHeader}>
                <h3>
                    <span className={styles.ruleLetter} aria-hidden="true">{ruleLetter(indicator.ruleCode)}</span>
                    {indicator.title}
                </h3>
                <span className={styles.ruleMeta}>{indicator.ruleVersion} · {formatDateTime(indicator.computedAt)}</span>
            </header>

            <p className={isEntryError ? styles.severityInfo : styles.severitySignal}>
                {isEntryError ? "Informativno – vjerojatna greška u izvoru" : "Vrijedi provjeriti"}
            </p>

            {indicator.sourceExplanation && (
                <section className={styles.sourceExplanation} aria-label="Objašnjenje naručitelja iz izvora">
                    <h4>Objašnjenje naručitelja iz izvora</h4>
                    <blockquote>„{indicator.sourceExplanation}”</blockquote>
                    <p className={styles.dimensionHint}>Doslovno iz stupca „Obrazloženje iznosa plaćanja” u registru ugovora.</p>
                </section>
            )}

            {/* A probable entry error is not headlined with its percentage (e.g. +12.163 %), which would mislead. */}
            {!isEntryError && <p className={styles.magnitude}>{indicator.magnitude.label}</p>}

            <div className={styles.dimensions}>
                <Dimension title="Kvaliteta podataka" value={dataQualityLabels[indicator.dataQuality]}
                           hint={dataQualityHints[indicator.dataQuality]}/>
                <Dimension title="Veličina odstupanja" value={indicator.magnitude.label ?? "nije izračunato"}
                           hint="Izračunato iz objavljenih iznosa, bez ispravaka."/>
                <Dimension title="Pouzdanost dokaza" value={evidenceStrengthLabels[indicator.evidenceStrength]}
                           hint={evidenceStrengthHints[indicator.evidenceStrength]}/>
            </div>

            <dl className={styles.indicatorDetails}>
                <dt>Što je izračunato</dt>
                <dd>{indicator.explanation}</dd>
                <dt>Što ovo NE znači</dt>
                <dd>{indicator.notMeaning}</dd>
                {indicator.missingData.length > 0 && (
                    <>
                        <dt>Što nedostaje</dt>
                        <dd>{indicator.missingData.join(", ")}</dd>
                    </>
                )}
                <dt>Dokazi</dt>
                <dd>
                    <ul className={styles.evidence}>
                        {indicator.evidence.map((link) => (
                            <li key={`${link.url}-${link.field ?? ""}`}>
                                <a href={link.url} target="_blank" rel="noreferrer">{link.label}</a>
                                {link.field && <span className={styles.muted}> – stupac „{link.field}”</span>}
                            </li>
                        ))}
                    </ul>
                </dd>
            </dl>
        </article>
    );
};
