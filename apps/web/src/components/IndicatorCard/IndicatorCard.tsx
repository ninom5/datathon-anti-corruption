import {Link} from "wouter";
import {feedbackPath, indicatorPath} from "@/routes";
import type {Indicator} from "@/types";
import {
    classNames,
    dataQualityHints,
    dataQualityLabels,
    evidenceStrengthHints,
    evidenceStrengthLabels,
    formatDateTime,
    formatMagnitude,
    ruleLetter,
} from "@/utils";
import {ExternalLink} from "@/components/ExternalLink";
import styles from "./IndicatorCard.module.css";

interface DimensionProps {
    title: string;
    value: string;
    hint: string;
    compact?: boolean;
}

/** One of the three equal boxes; there is never a combined score (docs/UI_UX.md §3.4). */
const Dimension = ({title, value, hint, compact}: DimensionProps) => (
    <div className={styles.dimension}>
        <h4>{title}</h4>
        <p className={styles.dimensionValue}>{value}</p>
        {!compact && <p className={styles.dimensionHint}>{hint}</p>}
    </div>
);

interface IndicatorCardProps {
    indicator: Indicator;
    showLink?: boolean;
    inContract?: boolean;
    compact?: boolean;
}

export const IndicatorCard = ({indicator, showLink = true, inContract = false, compact = false}: IndicatorCardProps) => {
    const isEntryError = indicator.possibleEntryError;
    const isInformative = indicator.severityNote === "informativno";
    const magnitude = compact && indicator.magnitude.value !== null ? formatMagnitude(indicator.ruleCode, indicator.magnitude.value) : indicator.magnitude.label ?? "nije izračunato";
    const severityLabel = isEntryError ? "Informativno – vjerojatna greška u izvoru" : isInformative ? "Informativno – poznato moguće objašnjenje" : "Vrijedi provjeriti";
    return (
        <article className={classNames(styles.indicator, isInformative && styles.indicatorInfo)}>
            <header className={styles.indicatorHeader}>
                <h3>
                    <span className={styles.ruleLetter} aria-hidden="true">{ruleLetter(indicator.ruleCode)}</span>
                    {indicator.title}
                </h3>
                <span className={styles.ruleMeta}>{indicator.ruleVersion} · {formatDateTime(indicator.computedAt)}</span>
            </header>

            <p className={isInformative ? styles.severityInfo : styles.severitySignal}>
                {severityLabel}
            </p>

            {indicator.sourceExplanation && (
                <section className={styles.sourceExplanation} aria-label="Objašnjenje naručitelja iz izvora">
                    <h4>Objašnjenje naručitelja iz izvora</h4>
                    <blockquote>„{indicator.sourceExplanation}”</blockquote>
                    <p className={styles.dimensionHint}>Doslovno iz stupca „Obrazloženje iznosa plaćanja” u registru ugovora.</p>
                </section>
            )}

            {/* A probable entry error is not headlined with its percentage (e.g. +12.163 %), which would mislead. */}
            {!compact && !isEntryError && <p className={styles.magnitude}>{indicator.magnitude.label}</p>}

            <div className={styles.dimensions}>
                <Dimension compact={compact} title="Kvaliteta podataka" value={dataQualityLabels[indicator.dataQuality]}
                           hint={dataQualityHints[indicator.dataQuality]}/>
                <Dimension compact={compact} title="Veličina odstupanja" value={compact && isEntryError ? "Moguća greška u izvoru" : magnitude}
                           hint={indicator.magnitude.band ? `Pojas: ${indicator.magnitude.band} (heuristika unutar pravila, a ne ukupna ocjena).` : "Izračunato iz objavljenih iznosa, bez ispravaka."}/>
                <Dimension compact={compact} title="Pouzdanost dokaza" value={evidenceStrengthLabels[indicator.evidenceStrength]}
                           hint={evidenceStrengthHints[indicator.ruleCode][indicator.evidenceStrength]}/>
            </div>

            <dl className={styles.indicatorDetails}>
                {!compact && <><dt>Što je izračunato</dt><dd>{indicator.explanation}</dd></>}
                <dt>Što ovo NE znači</dt>
                <dd>{indicator.notMeaning}</dd>
                {!compact && indicator.missingData.length > 0 && (
                    <>
                        <dt>Što nedostaje</dt>
                        <dd>{indicator.missingData.join(", ")}</dd>
                    </>
                )}
                {!compact && <><dt>Dokazi</dt>
                <dd>
                    <ul className={styles.evidence}>
                        {indicator.evidence.map((link) => (
                            <li key={`${link.url}-${link.field ?? ""}-${link.label}`}>
                                <ExternalLink href={link.url}>{link.label}</ExternalLink>
                                {link.field && <span className={styles.muted}> – stupac „{link.field}”</span>}
                            </li>
                        ))}
                    </ul>
                </dd>
                </>}
            </dl>
            {inContract && indicator.subjectType !== "contract" && <p>Ovaj ugovor je dio skupine od {indicator.contractIds.length} ugovora.</p>}
            {(showLink || !compact) && (
                <p className={styles.links}>
                    {showLink && <Link href={indicatorPath(indicator.id)}>Otvori indikator →</Link>}
                    {!compact && <Link href={feedbackPath("indikator", indicator.id)}>Prijavi grešku ili dodaj kontekst</Link>}
                </p>
            )}
        </article>
    );
};
