import type {ReactNode} from "react";
import styles from "./QueryStates.module.css";

interface LoadingStateProps {
    label: string;
}

export const LoadingState = ({label}: LoadingStateProps) => (
    <div className={styles.box} role="status" aria-live="polite">
        <span className={styles.spinner} aria-hidden="true"/>
        {label}
    </div>
);

interface ErrorStateProps {
    message: string;
    onRetry: () => void;
}

export const ErrorState = ({message, onRetry}: ErrorStateProps) => (
    <div className={`${styles.box} ${styles.error}`} role="alert">
        <p className={styles.title}>Podaci se nisu učitali.</p>
        <p>{message}</p>
        <button type="button" className={styles.button} onClick={onRetry}>Pokušaj ponovno</button>
    </div>
);

interface EmptyStateProps {
    title: string;
    children?: ReactNode;
}

export const EmptyState = ({title, children}: EmptyStateProps) => (
    <div className={styles.box}>
        <p className={styles.title}>{title}</p>
        {children}
    </div>
);
