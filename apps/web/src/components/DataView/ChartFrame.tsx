import type {ReactNode} from "react";
import styles from "./DataView.module.css";

interface ChartFrameProps {
    title: string;
    description: ReactNode;
    children: ReactNode;
    table: ReactNode;
}

export const ChartFrame = ({title, description, children, table}: ChartFrameProps) => (
    <section className={styles.section}>
        <h2>{title}</h2>
        <p className={styles.note}>{description}</p>
        {children}
        <details className={styles.disclosure}>
            <summary>Prikaži kao tablicu</summary>
            <div className={styles.tableWrap} tabIndex={0} role="region" aria-label={`Tablica: ${title}`}>{table}</div>
        </details>
    </section>
);
