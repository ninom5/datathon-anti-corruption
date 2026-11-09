import {useSearchParams} from "wouter";
import {Button, EmptyState, ErrorState, LoadingState, PageHeader, SyntheticBanner} from "@/components";
import {useBuyers, useContracts, useContractSummary, useSourcesSummary} from "@/hooks";
import type {ContractListItem, YearCount} from "@/types";
import {ContractTable} from "./ContractTable.tsx";
import {type ExplorerState, PAGE_SIZE, readExplorerState, toContractFilter, writeExplorerState} from "./explorerState.ts";
import {FilterPanel} from "./FilterPanel.tsx";
import {Pagination} from "./Pagination.tsx";
import {SummaryBar} from "./SummaryBar.tsx";
import styles from "./ContractsPage.module.css";

const selectableYears = (years: YearCount[], selected: number | undefined) => {
    const options = years.map(({year, incomplete}) => ({year, incomplete}));
    if (selected && !options.some((option) => option.year === selected)) {
        options.push({year: selected, incomplete: false});
    }
    return options.sort((a, b) => b.year - a.year);
};

interface ResultsProps {
    contracts: ContractListItem[] | undefined;
    totalElements: number;
    isPending: boolean;
    error: Error | null;
    onRetry: () => void;
    onReset: () => void;
    onFirstPage: () => void;
}

const Results = ({contracts, totalElements, isPending, error, onRetry, onReset, onFirstPage}: ResultsProps) => {
    if (error) {
        return <ErrorState message={error.message} onRetry={onRetry}/>;
    }
    if (isPending || !contracts) {
        return <LoadingState message="Učitavanje ugovora…"/>;
    }
    if (contracts.length === 0 && totalElements > 0) {
        return (
            <EmptyState
                title="Ova stranica rezultata ne postoji"
                hint="Poveznica vodi na stranicu iza zadnje. Rezultati za odabrane filtre postoje na ranijim stranicama."
                actions={<Button variant="secondary" onClick={onFirstPage}>Prva stranica</Button>}
            />
        );
    }
    if (contracts.length === 0) {
        return (
            <EmptyState
                title="Nema ugovora za odabrane filtre"
                hint="Proširite odabir, npr. uklonite godinu ili oznaku pravila."
                actions={<Button variant="secondary" onClick={onReset}>Poništi filtre</Button>}
            />
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
    const handlePageChange = (page: number) => {
        handleChange({page});
        window.scrollTo(0, 0);
    };

    return (
        <>
            {state.synthetic && <SyntheticBanner/>}
            <PageHeader
                title="Istraživač ugovora"
                description="Ugovori iz registara koje naručitelji objavljuju na data.gov.hr. Oznaka pravila znači da nešto vrijedi provjeriti, a ne da je nešto pogrešno."
            />
            {summary.error && <ErrorState message={summary.error.message} onRetry={() => summary.refetch()}/>}
            {!summary.error && summary.data && <SummaryBar summary={summary.data} isSyntheticShown={state.synthetic}/>}
            <div className={styles.layout}>
                <FilterPanel
                    key={state.supplierOib ?? ""}
                    state={state}
                    ruleCounts={summary.isFetching || summary.error ? undefined : summary.data?.contractsByRule}
                    buyers={buyers.data ?? []}
                    years={selectableYears(coverage.data?.contractsByYear ?? [], state.year)}
                    onChange={handleChange}
                    onReset={handleReset}
                />
                <section className={styles.results} aria-label="Rezultati" aria-busy={contracts.isFetching}>
                    <Results
                        contracts={contracts.data?.content}
                        totalElements={contracts.data?.totalElements ?? 0}
                        isPending={contracts.isPending}
                        error={contracts.error}
                        onRetry={() => contracts.refetch()}
                        onReset={handleReset}
                        onFirstPage={() => handlePageChange(0)}
                    />
                    {contracts.data && contracts.data.content.length > 0 && (
                        <>
                            <p className={styles.footnote}>* nepotpuna godina: registri su dohvaćeni prije kraja godine.</p>
                            <Pagination
                                page={contracts.data.page}
                                size={PAGE_SIZE}
                                totalElements={contracts.data.totalElements}
                                onPageChange={handlePageChange}
                            />
                        </>
                    )}
                </section>
            </div>
        </>
    );
};
