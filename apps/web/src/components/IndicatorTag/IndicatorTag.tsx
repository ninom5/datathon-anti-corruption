import type {IndicatorBadge} from "@/types";
import {classNames, formatMagnitude, ruleLetter} from "@/utils";
import styles from "./IndicatorTag.module.css";

interface IndicatorTagProps {
    indicator: Pick<IndicatorBadge, "ruleCode" | "magnitudeValue" | "severityNote" | "possibleEntryError">;
}

export const IndicatorTag = ({indicator}: IndicatorTagProps) => {
    const letter = ruleLetter(indicator.ruleCode);
    if (indicator.possibleEntryError) {
        return (
            <span className={classNames(styles.tag, styles.entryError)} title="Informativno: vjerojatna greška u izvoru">
                {letter} · moguća greška u unosu
            </span>
        );
    }
    const magnitude = indicator.magnitudeValue === null ? "" : ` ${formatMagnitude(indicator.ruleCode, indicator.magnitudeValue)}`;
    if (indicator.severityNote === "informativno") {
        return (
            <span className={classNames(styles.tag, styles.informative)} title="Informativno: poznato moguće objašnjenje (npr. vlastito komunalno društvo)">
                {letter}{magnitude} · informativno
            </span>
        );
    }
    return (
        <span className={classNames(styles.tag, styles.signal)} title="Vrijedi provjeriti">
            {letter}{magnitude}
        </span>
    );
};
