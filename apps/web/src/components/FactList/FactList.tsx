import type {ReactNode} from "react";
import {classNames} from "@/utils";
import styles from "./FactList.module.css";

export interface Fact {
    label: string;
    value: ReactNode;
    isAi?: boolean;
}

interface FactListProps {
    facts: Fact[];
    className?: string;
}

export const FactList = ({facts, className}: FactListProps) => {
    return (
        <dl className={classNames(styles.list, className)}>
            {facts.map(({label, value, isAi}) => (
                <div key={label} className={styles.row}>
                    <dt className={styles.label}>{label}</dt>
                    <dd className={styles.value}>
                        {isAi ? <span className={styles.aiValue}>{value}</span> : value}
                    </dd>
                </div>
            ))}
        </dl>
    );
};

interface UnknownValueProps {
    children?: ReactNode;
}

export const UnknownValue = ({children = "nije objavljeno"}: UnknownValueProps) => (
    <span className={styles.unknown}>{children}</span>
);
