import {Link} from "wouter";
import {ErrorState, LoadingState} from "@/components";
import {useContractSummary, useSourcesSummary} from "@/hooks";
import {routes} from "@/routes";
import {formatEurMillions, formatNumber, plural} from "@/utils";
import styles from "./HomePage.module.css";

const NO_FILTER = {};

export const HomePage = () => {
    const summary = useContractSummary(NO_FILTER);
    const coverage = useSourcesSummary();

    const renderFacts = () => {
        if (summary.error) {
            return <ErrorState message={summary.error.message} onRetry={() => summary.refetch()}/>;
        }
        if (summary.isPending || !coverage.data) {
            return <LoadingState label="Učitavanje brojki…"/>;
        }
        const jls = coverage.data.buyersByType.jls ?? 0;
        const otherBuyers = Object.values(coverage.data.buyersByType).reduce((sum, count) => sum + count, 0) - jls;
        return (
            <ol className={styles.facts}>
                <li>
                    <strong className="tabular">{formatNumber(coverage.data.contracts)}</strong> ugovora
                    iz {formatNumber(jls)} {plural(jls, "općine ili grada", "općine ili grada", "općina i gradova")}
                    {otherBuyers > 0 && <> te {formatNumber(otherBuyers)} {plural(otherBuyers, "drugog naručitelja", "druga naručitelja", "drugih naručitelja")}</>}.
                    Bez ugovora s obrtima to je ukupno <span className="tabular">{formatEurMillions(summary.data.totalValue)}</span> bez PDV-a.
                    <Link href={routes.CONTRACTS}>Istraži sve ugovore</Link>
                </li>
                <li>
                    <strong className={`tabular ${styles.signal}`}>{formatNumber(summary.data.worthChecking)}</strong>{" "}
                    {plural(summary.data.worthChecking, "ugovor", "ugovora", "ugovora")} s povećanjem vrijednosti ili
                    isplatom iznad ugovorenog koji vrijedi provjeriti.
                    <Link href={`${routes.CONTRACTS}?oznake=worth`}>Pogledaj</Link>
                </li>
                <li>
                    <strong className="tabular">{formatNumber(summary.data.possibleEntryErrors)}</strong>{" "}
                    {plural(summary.data.possibleEntryErrors, "moguća greška", "moguće greške", "mogućih grešaka")} u
                    unosu u objavljenim registrima, koje naručitelji mogu ispraviti.
                    <Link href={`${routes.CONTRACTS}?oznake=error`}>Pogledaj</Link>
                </li>
            </ol>
        );
    };

    return (
        <div className={styles.page}>
            <section className={styles.hero}>
                <h1>Kamo odlazi novac vaše općine?</h1>
                <p className={styles.lead}>
                    JavniRadar čita registre ugovora koje općine, gradovi i županije objavljuju kao otvorene podatke i
                    pokazuje što u njima vrijedi provjeriti. Svaka brojka vodi do retka u izvornoj datoteci.
                </p>
            </section>
            <section aria-labelledby="facts-title">
                <h2 id="facts-title" className={styles.factsTitle}>Što se vidi u podacima</h2>
                {renderFacts()}
            </section>
            <p className={styles.disclaimer}>
                Oznake nisu optužbe. Pokazuju neuobičajene obrasce u objavljenim brojkama i uvijek navode što ne znače.{" "}
                <Link href={routes.SOURCES}>Izvori i metodologija</Link>
            </p>
        </div>
    );
};
