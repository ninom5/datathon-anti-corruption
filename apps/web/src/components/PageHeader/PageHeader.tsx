import type {ReactNode} from "react";
import styles from "./PageHeader.module.css";

interface PageHeaderProps {
    title: string;
    description?: string;
    meta?: ReactNode;
    actions?: ReactNode;
}

export const PageHeader = ({title, description, meta, actions}: PageHeaderProps) => {
    return (
        <header className={styles.header}>
            <div className={styles.text}>
                <h1 className={styles.title}>{title}</h1>
                {description && <p className={styles.description}>{description}</p>}
                {meta && <div className={styles.meta}>{meta}</div>}
            </div>
            {actions && <div className={styles.actions}>{actions}</div>}
        </header>
    );
};
