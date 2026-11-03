import {Link, useSearchParams} from "wouter";
import {EmptyState, ErrorState, LoadingState} from "@/components";
import {useMunicipalities, usePeerComparison} from "@/hooks";
import {mapPath, routes} from "@/routes";
import type {PeerComparison} from "@/types";
import {formatEur, formatNumber} from "@/utils";
import {ComparisonFilters} from "./ComparisonFilters.tsx";
import {type ComparisonState, readComparisonState, writeComparisonState} from "./comparisonState.ts";
import {DotPlot} from "./DotPlot.tsx";
import {PeerTable} from "./PeerTable.tsx";
import styles from "./ComparisonPage.module.css";

const categoryText = (comparison: PeerComparison) =>
    comparison.cpv2 ? `na kategoriju ${comparison.cpv2} ${comparison.cpvName}` : "ukupno na sve ugovore";

/**
 * "GRAD X: u 2025. ugovoreno 41 € po stanovniku ...", neutral wording from docs/ANOMALY_DETECTION.md §3A. Impersonal
 * so it fits both "grad" and "općina" without grammatical gender.
 */
const headline = (comparison: PeerComparison) => {
    const selected = comparison.members.find((member) => member.selected);
    const name = comparison.municipality.name;
    if (!selected) {
        return `${name} nema objavljenih ugovora sklopljenih ${comparison.year}., pa nije dio usporedbe za tu godinu.`;
    }
    const median = comparison.medianPerCapita === null ? "" :
        ` Medijan skupine (${comparison.peerGroupLabel}, n = ${comparison.withData}) je ${formatEur(comparison.medianPerCapita)}.`;
    return `${name}: u ${comparison.year}. ugovoreno je ${formatEur(selected.valuePerCapita)} po stanovniku ${categoryText(comparison)}`
        + ` (${comparison.selectedRank}. od ${comparison.withData}).${median}`;
};

interface ComparisonResultProps {
    comparison: PeerComparison;
}

const ComparisonResult = ({comparison}: ComparisonResultProps) => {
    if (comparison.peerGroup === "P7") {
        return <EmptyState title="Grad Zagreb nema usporedive skupine.">
            <p>Usporedba po stanovniku ima smisla samo među sličnim JLS, a Zagreb je jedini u svojoj skupini.</p>
        </EmptyState>;
    }
    if (comparison.withData === 0) {
        return <EmptyState title={`Nijedna JLS iz skupine nema ugovora za ${comparison.year}.`}>
            <p>Odaberite drugu godinu.</p>
        </EmptyState>;
    }
    return (
        <>
            <p className={styles.headline}>{headline(comparison)}</p>
            {!comparison.sufficient && (
                <p className={styles.warning} role="note">
                    <strong>Nedovoljno podataka za pouzdanu usporedbu.</strong> Strojno čitljiv registar za
                    {" "}{comparison.year}. objavljuje {formatNumber(comparison.withData)} od {formatNumber(comparison.groupSize)} JLS
                    iz skupine, a za usporedbu treba barem {comparison.minimumForComparison}. Brojke su prikazane radi
                    transparentnosti, ne kao mjerilo.
                </p>
            )}
            {comparison.yearIncomplete && (
                <p className={styles.warning} role="note">{comparison.year}. još nije završila, pa su iznosi nepotpuni.</p>
            )}
            <DotPlot members={comparison.members} median={comparison.medianPerCapita}
                     summary={`Potrošnja po stanovniku ${categoryText(comparison)} u ${comparison.year}. za ${comparison.withData} JLS iz skupine. ${headline(comparison)}`}/>
            <p className={styles.note}>Usporedba kategorija, ne jediničnih cijena.</p>
            <section className={styles.notMeaning} aria-labelledby="not-meaning-title">
                <h2 id="not-meaning-title">Što ovo NE znači</h2>
                <p>
                    Viša potrošnja po stanovniku ne znači da je nešto preplaćeno. Česti razlozi su jednokratne investicije
                    (npr. obnova škole ili ceste u jednoj godini), ugovori koji pokrivaju više godina, turizam (više
                    korisnika od broja stanovnika) ili drugačije odabran CPV. JLS bez strojno čitljivog registra nisu u
                    usporedbi i nisu računate kao nula.
                </p>
            </section>
            <h2 className={styles.sectionTitle}>Tablica</h2>
            <PeerTable members={comparison.members} isAllCategories={comparison.cpv2 === null}/>
            <p className={styles.source}>
                Izvori: registri ugovora JLS s data.gov.hr (ukupna vrijednost s izmjenama, bez PDV-a; bez ugovora s
                fizičkim osobama), broj stanovnika DZS Popis 2021. Detalji su na stranici{" "}
                <Link href={routes.SOURCES}>Izvori</Link>.
            </p>
        </>
    );
};

export const ComparisonPage = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const state = readComparisonState(searchParams);
    const municipalities = useMunicipalities();
    const comparison = usePeerComparison(state.oib, {year: state.year, cpv2: state.cpv2});
    const withData = municipalities.data?.filter((municipality) => municipality.contracts > 0) ?? [];

    const update = (next: ComparisonState) => setSearchParams(writeComparisonState(next));

    const renderResult = () => {
        if (!state.oib) {
            return <EmptyState title="Odaberite JLS za usporedbu.">
                <p>Usporedba je moguća za {formatNumber(withData.length)} JLS koje objavljuju strojno čitljiv registar ugovora.</p>
            </EmptyState>;
        }
        if (comparison.error) {
            return <ErrorState message={comparison.error.message} onRetry={() => comparison.refetch()}/>;
        }
        if (comparison.isPending) {
            return <LoadingState label="Učitavanje usporedbe…"/>;
        }
        return <ComparisonResult comparison={comparison.data}/>;
    };

    return (
        <>
            <header className={styles.pageHeader}>
                <h1>Usporedba sa sličnim JLS</h1>
                <p className={styles.lead}>
                    Potrošnja po stanovniku u odnosu na JLS slične veličine (usporedne skupine po Popisu 2021.). Uspoređuju
                    se samo JLS koje imaju podatke za odabranu godinu.
                    {state.oib && <> <Link href={mapPath(state.oib)}>Prikaži na karti</Link></>}
                </p>
            </header>
            {municipalities.error && <ErrorState message={municipalities.error.message} onRetry={() => municipalities.refetch()}/>}
            <ComparisonFilters
                municipalities={withData}
                selectedOib={state.oib}
                comparison={comparison.data}
                onChangeMunicipality={(oib) => update({oib, year: state.year})}
                onChangeYear={(year) => update({...state, year})}
                onChangeCategory={(cpv2) => update({...state, cpv2})}/>
            <div className={comparison.isPlaceholderData ? styles.stale : undefined}>{renderResult()}</div>
        </>
    );
};
