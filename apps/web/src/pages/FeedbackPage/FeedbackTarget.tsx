import {Link} from "wouter";
import {contractPath, indicatorPath} from "@/routes";
import type {ContractDetail, FeedbackTargetType, IndicatorDetail} from "@/types";
import {formatDate, ruleLetter} from "@/utils";
import styles from "./FeedbackPage.module.css";

interface FeedbackTargetProps {
    targetType: FeedbackTargetType;
    indicator: IndicatorDetail | undefined;
    contract: ContractDetail | undefined;
}

export const FeedbackTarget = ({targetType, indicator, contract}: FeedbackTargetProps) => {
    if (targetType === "indicator" && indicator) {
        return (
            <p className={styles.target}>
                Indikator {ruleLetter(indicator.ruleCode)}:{" "}
                <Link href={indicatorPath(indicator.id)}>{indicator.title}</Link>
                {indicator.buyer && <> · {indicator.buyer.name}</>}
            </p>
        );
    }
    if (targetType === "contract" && contract) {
        return (
            <p className={styles.target}>
                Ugovor: <Link href={contractPath(contract.id)}>{contract.subject}</Link>
                {" "}· {contract.buyer.name} · <span className="tabular-nums">{formatDate(contract.dateSigned)}</span>
            </p>
        );
    }
    return null;
};
