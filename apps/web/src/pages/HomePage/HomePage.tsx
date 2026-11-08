import {Link} from "wouter";
import {ErrorState, LoadingState, PageHeader} from "@/components";
import {useContractSummary, useSourcesSummary} from "@/hooks";
import {routes} from "@/routes";
import {formatEurMillions, formatNumber, plural} from "@/utils";
import {HowItWorks} from "./HowItWorks.tsx";
import styles from "./HomePage.module.css";

const NO_FILTER = {};

export const HomePage = () => {
    const summary = useContractSummary(NO_FILTER);
    const coverage = useSourcesSummary();

    const renderFacts = () => {
        const error = summary.error ?? coverage.error;
        if (error) {
            return <ErrorState message={error.message} onRetry={() => {
                void summary.refetch();
                void coverage.refetch();
            }}/>;
        }
        if (!summary.data || !coverage.data) {
            return <LoadingState message="Učitavanje brojki…"/>;
        }
        const jls = coverage.data.buyersByType.jls ?? 0;
        const otherBuyers = Object.values(coverage.data.buyersByType).reduce((sum, count) => sum + count, 0) - jls;
        // The summary counts the contracts shown in the explorer (crafts included, contracts with natural persons left out).
        return (
            <ol className={styles.facts}>
                <li>
                    <strong className="tabular-nums">{formatNumber(summary.data.contracts)}</strong>{" "}
                    {plural(summary.data.contracts, "ugovor", "ugovora", "ugovora")}
                    {" "}iz {formatNumber(jls)} {plural(jls, "općine ili grada", "općine ili grada", "općina i gradova")}
                    {otherBuyers > 0 && <> te {formatNumber(otherBuyers)} {plural(otherBuyers, "drugog naručitelja", "druga naručitelja", "drugih naručitelja")}</>},
                    ukupno <span className="tabular-nums">{formatEurMillions(summary.data.totalValue)}</span> bez PDV-a.
                    <Link href={routes.CONTRACTS}>Istraži sve ugovore</Link>
                </li>
                <li>
                    <strong className={`tabular-nums ${styles.signal}`}>{formatNumber(summary.data.worthChecking)}</strong>{" "}
                    {plural(summary.data.worthChecking, "ugovor", "ugovora", "ugovora")} s barem jednom oznakom pravila
                    (izmjene i isplate, moguće cjepkanje, koncentracija dobavljača) koju vrijedi provjeriti.
                    <Link href={`${routes.CONTRACTS}?oznake=worth`}>Pogledaj</Link>
                </li>
                <li>
                    <strong className="tabular-nums">{formatNumber(summary.data.possibleEntryErrors)}</strong>{" "}
                    {plural(summary.data.possibleEntryErrors, "moguća greška", "moguće greške", "mogućih grešaka")} u
                    unosu u objavljenim registrima, koje naručitelji mogu ispraviti.
                    <Link href={`${routes.CONTRACTS}?oznake=error`}>Pogledaj</Link>
                </li>
            </ol>
        );
    };

    return (
        <>
            <PageHeader
                title="Kamo odlazi novac vaše općine?"
                description="JavniRadar čita registre ugovora koje općine, gradovi i županije objavljuju kao otvorene podatke i pokazuje što u njima vrijedi provjeriti. Svaka brojka vodi do retka u izvornoj datoteci."
            />
            <div className={styles.layout}>
                <section className={styles.main} aria-labelledby="facts-title">
                    <h2 id="facts-title" className={styles.factsTitle}>Što se vidi u podacima</h2>
                    {renderFacts()}
                    <p className={styles.disclaimer}>
                        Oznake nisu optužbe. Pokazuju neuobičajene obrasce u objavljenim brojkama i uvijek navode što
                        ne znače.{" "}
                        <Link href={routes.SOURCES}>Izvori i metodologija</Link>
                    </p>
                </section>
                <aside className={styles.aside}>
                    <HowItWorks/>
                </aside>
            </div>
        </>
    );
};
