import type {IndicatorBadge} from "@/types";
import {formatMagnitude, ruleLetter} from "@/utils";
import styles from "./IndicatorTag.module.css";

interface IndicatorTagProps {
    indicator: Pick<IndicatorBadge, "ruleCode" | "magnitudeValue" | "severityNote" | "possibleEntryError">;
}

/**
 * Small badge with the rule letter, so colour is never the only carrier of meaning. A probable entry error shows no
 * percentage: numbers like +12.000 % come from typos in the source and would mislead. "Informativno" F and C
 * indicators (the buyer's own company, recurring supplies) are context, not errors, so they are shown without the signal.
 */
export const IndicatorTag = ({indicator}: IndicatorTagProps) => {
    const letter = ruleLetter(indicator.ruleCode);
    if (indicator.possibleEntryError) {
        return (
            <span className={`${styles.tag} ${styles.entryError}`} title="Informativno: vjerojatna greška u izvoru">
                {letter} · moguća greška u unosu
            </span>
        );
    }
    const magnitude = indicator.magnitudeValue === null ? "" : ` ${formatMagnitude(indicator.ruleCode, indicator.magnitudeValue)}`;
    if (indicator.severityNote === "informativno") {
        return (
            <span className={`${styles.tag} ${styles.informative}`} title="Informativno: poznato moguće objašnjenje (npr. vlastito komunalno društvo)">
                {letter}{magnitude} · informativno
            </span>
        );
    }
    return (
        <span className={`${styles.tag} ${styles.signal}`} title="Vrijedi provjeriti">
            {letter}{magnitude}
        </span>
    );
};
