import styles from "./StatePanel.module.css";

interface LoadingStateProps {
    message?: string;
}

export const LoadingState = ({message = "Učitavanje…"}: LoadingStateProps) => {
    return (
        <div className={styles.panel} role="status" aria-live="polite">
            <span className={styles.spinner} aria-hidden="true"/>
            <p className={styles.message}>{message}</p>
        </div>
    );
};
