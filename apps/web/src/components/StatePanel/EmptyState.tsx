import type {ReactNode} from "react";
import {Icon, type IconName} from "@/components/Icon";
import styles from "./StatePanel.module.css";

interface EmptyStateProps {
    title: string;
    hint?: string;
    icon?: IconName;
    actions?: ReactNode;
}

export const EmptyState = ({title, hint, icon = "search", actions}: EmptyStateProps) => {
    return (
        <div className={styles.panel}>
            <span className={styles.icon}>
                <Icon name={icon} size={20}/>
            </span>
            <h2 className={styles.title}>{title}</h2>
            {hint && <p className={styles.message}>{hint}</p>}
            {actions && <div className={styles.actions}>{actions}</div>}
        </div>
    );
};
