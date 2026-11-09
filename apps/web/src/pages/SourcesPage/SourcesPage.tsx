import {useState} from "react";
import {Link} from "wouter";
import {Button, EmptyState, ErrorState, LoadingState, PageHeader} from "@/components";
import {useSources, useSourcesSummary} from "@/hooks";
import {routes} from "@/routes";
import type {SourceDataset} from "@/types";
import {formatNumber} from "@/utils";
import {CoverageSummary} from "./CoverageSummary.tsx";
import {DatasetTable} from "./DatasetTable.tsx";
import styles from "./SourcesPage.module.css";

const PAGE_SIZE = 100;

const hasImportedFile = (dataset: SourceDataset) => dataset.files.some((file) => file.status === "uvezeno");

export const SourcesPage = () => {
    const [page, setPage] = useState(0);
    const [isImportedOnly, setIsImportedOnly] = useState(false);
    const sources = useSources(page, PAGE_SIZE);
    const summary = useSourcesSummary();

    const renderDatasets = () => {
        if (sources.error) {
            return <ErrorState message={sources.error.message} onRetry={() => sources.refetch()}/>;
        }
        if (sources.isPending) {
            return <LoadingState message="Učitavanje izvora…"/>;
        }
        const datasets = isImportedOnly ? sources.data.content.filter(hasImportedFile) : sources.data.content;
        if (datasets.length === 0) {
            return <EmptyState icon="database" title="Nema skupova za prikaz" hint="Pokrenite dohvat podataka ili isključite filtar."/>;
        }
        return <DatasetTable datasets={datasets}/>;
    };

    const pageCount = sources.data ? Math.ceil(sources.data.totalElements / PAGE_SIZE) : 1;

    return (
        <>
            <PageHeader
                title="Izvori podataka"
                description="Svaki ugovor u JavniRadaru vodi do datoteke i retka iz kojeg je pročitan. Podaci se preuzimaju s Portala otvorenih podataka (data.gov.hr) uz poštivanje robots.txt-a; eojn.hr se ne pretražuje automatski."
            />

            {summary.error && <ErrorState message={summary.error.message} onRetry={() => summary.refetch()}/>}
            {summary.isPending && <LoadingState message="Učitavanje pokrivenosti…"/>}
            {summary.data && <CoverageSummary summary={summary.data}/>}

            <section aria-labelledby="datasets-title" className={styles.datasets}>
                <div className={styles.datasetsHeader}>
                    <h2 id="datasets-title">
                        Skupovi podataka{sources.data && ` (${formatNumber(sources.data.totalElements)})`}
                    </h2>
                    <label className={styles.toggle}>
                        <input type="checkbox" checked={isImportedOnly} onChange={(event) => setIsImportedOnly(event.target.checked)}/>
                        Samo skupovi s uvezenim ugovorima
                    </label>
                </div>
                {renderDatasets()}
                {pageCount > 1 && (
                    <nav className={styles.pager} aria-label="Stranice skupova">
                        <Button variant="secondary" disabled={page === 0} onClick={() => setPage(page - 1)}>Prethodna</Button>
                        <span className="tabular-nums">{page + 1} / {pageCount}</span>
                        <Button variant="secondary" disabled={page + 1 >= pageCount} onClick={() => setPage(page + 1)}>Sljedeća</Button>
                    </nav>
                )}
            </section>

            <p className={styles.methodLink}>
                Kako se iz ovih podataka računaju oznake, s pragovima i ograničenjima, piše u{" "}
                <Link href={routes.METHODOLOGY}>metodologiji</Link>.
            </p>
        </>
    );
};
