import type {FallbackProps} from "react-error-boundary";
import styles from "./ErrorPage.module.css";

export const ErrorPage = ({resetErrorBoundary}: FallbackProps) => (
    <div role="alert" className={styles.page}>
        <h1>Nešto je pošlo po zlu.</h1>
        <p>Stranica se nije mogla prikazati. Pokušajte ponovno ili se vratite na početnu.</p>
        <button type="button" className={styles.button} onClick={resetErrorBoundary}>Pokušaj ponovno</button>
    </div>
);
