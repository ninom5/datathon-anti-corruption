import {classNames} from "@/utils";
import {Button} from "@/components/Button";
import {Icon} from "@/components/Icon";
import styles from "./StatePanel.module.css";

interface ErrorStateProps {
    title?: string;
    message?: string;
    onRetry?: () => void;
}

export const ErrorState = ({
    title = "Nešto nije u redu",
    message = "Podatke trenutno nije moguće učitati.",
    onRetry,
}: ErrorStateProps) => {
    return (
        <div className={styles.panel} role="alert">
            <span className={classNames(styles.icon, styles.iconDanger)}>
                <Icon name="alert" size={20}/>
            </span>
            <h2 className={styles.title}>{title}</h2>
            <p className={styles.message}>{message}</p>
            {onRetry && (
                <div className={styles.actions}>
                    <Button variant="secondary" onClick={onRetry}>
                        <Icon name="retry"/>
                        Pokušaj ponovno
                    </Button>
                </div>
            )}
        </div>
    );
};
