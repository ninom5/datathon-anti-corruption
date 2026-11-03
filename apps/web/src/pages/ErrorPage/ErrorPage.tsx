import type {FallbackProps} from "react-error-boundary";
import {ErrorState} from "@/components";
import styles from "./ErrorPage.module.css";

export const ErrorPage = ({resetErrorBoundary}: FallbackProps) => (
    <div className={styles.page}>
        <ErrorState
            title="Nešto je pošlo po zlu"
            message="Stranica se nije mogla prikazati. Pokušajte ponovno ili se vratite na početnu."
            onRetry={resetErrorBoundary}
        />
    </div>
);
