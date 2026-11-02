import {useSearchParams} from "wouter";
import {EmptyState, ErrorState, LoadingState, SyntheticBanner} from "@/components";
import {useBuyers, useContracts, useContractSummary, useSourcesSummary} from "@/hooks";
import type {ContractListItem, YearCount} from "@/types";
import {ContractTable} from "./ContractTable.tsx";
import {type ExplorerState, PAGE_SIZE, readExplorerState, toContractFilter, writeExplorerState} from "./explorerState.ts";
import {FilterPanel} from "./FilterPanel.tsx";
import {Pagination} from "./Pagination.tsx";
import {SummaryBar} from "./SummaryBar.tsx";
import styles from "./ContractsPage.module.css";

/** Synthetic rows carry 2022 (a kuna contract) and 2026, so selectable years are the real ones plus those. */
const selectableYears = (years: YearCount[], selected: number | undefined) => {
    const options = years.map(({year, incomplete}) => ({year, incomplete}));
    if (selected && !options.some((option) => option.year === selected)) {
        options.push({year: selected, incomplete: false});
    }
    return options.sort((a, b) => b.year - a.year);
};

interface ResultsProps {
    contracts: ContractListItem[] | undefined;
    isPending: boolean;
    error: Error | null;
    onRetry: () => void;
    onReset: () => void;
}

const Results = ({contracts, isPending, error, onRetry, onReset}: ResultsProps) => {
    if (error) {
        return <ErrorState message={error.message} onRetry={onRetry}/>;
    }
    if (isPending || !contracts) {
        return <LoadingState label="Učitavanje ugovora…"/>;
    }
    if (contracts.length === 0) {
        return (
            <EmptyState title="Nema ugovora za odabrane filtre.">
                <p>Proširite odabir, npr. uklonite godinu ili oznaku pravila.</p>
                <button type="button" className={styles.linkButton} onClick={onReset}>Poništi filtre</button>
            </EmptyState>
        );
    }
    return <ContractTable contracts={contracts}/>;
};

export const ContractsPage = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const state = readExplorerState(searchParams);
    const filter = toContractFilter(state);
    const contracts = useContracts(filter);
    const summary = useContractSummary(filter);
    const buyers = useBuyers(state.synthetic);
    const coverage = useSourcesSummary();

    const handleChange = (next: Partial<ExplorerState>) =>
        setSearchParams(writeExplorerState({...state, ...next, page: next.page ?? 0}));
    const handleReset = () => setSearchParams({});

    return (
        <>
            {state.synthetic && <div className={styles.bleed}><SyntheticBanner/></div>}
            <header className={styles.pageHeader}>
                <h1>Istraživač ugovora</h1>
                <p className={styles.lead}>
                    Ugovori iz registara koje naručitelji objavljuju na data.gov.hr. Oznaka pravila znači da nešto
                    vrijedi provjeriti, a ne da je nešto pogrešno.
                </p>
            </header>
            {summary.data && <SummaryBar summary={summary.data} isSyntheticShown={state.synthetic}/>}
            <div className={styles.layout}>
                <FilterPanel
                    state={state}
                    buyers={buyers.data ?? []}
                    years={selectableYears(coverage.data?.contractsByYear ?? [], state.year)}
                    onChange={handleChange}
                    onReset={handleReset}
                />
                <section className={styles.results} aria-label="Rezultati" aria-busy={contracts.isFetching}>
                    <Results
                        contracts={contracts.data?.content}
                        isPending={contracts.isPending}
                        error={contracts.error}
                        onRetry={() => contracts.refetch()}
                        onReset={handleReset}
                    />
                    {contracts.data && contracts.data.totalElements > 0 && (
                        <>
                            <p className={styles.footnote}>* nepotpuna godina: registri su dohvaćeni prije kraja godine.</p>
                            <Pagination
                                page={contracts.data.page}
                                size={PAGE_SIZE}
                                totalElements={contracts.data.totalElements}
                                onPageChange={(page) => handleChange({page})}
                            />
                        </>
                    )}
                </section>
            </div>
        </>
    );
};
