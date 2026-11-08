import {useState} from "react";
import {Button, EmptyState, ErrorState, LoadingState, PageHeader} from "@/components";
import {useSources, useSourcesSummary} from "@/hooks";
import type {SourceDataset} from "@/types";
import {formatNumber} from "@/utils";
import {CoverageSummary} from "./CoverageSummary.tsx";
import {DatasetTable} from "./DatasetTable.tsx";
import styles from "./SourcesPage.module.css";

/** All datasets fit on one page (the API caps a page at 100). */
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
                title="Izvori i metodologija"
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

            <section aria-labelledby="method-title" className={styles.method}>
                <h2 id="method-title">Kako se računaju oznake</h2>
                <ul>
                    <li>
                        <strong>D – povećanje vrijednosti ugovora:</strong> ukupni iznos s izmjenama u odnosu na početni,
                        uz pragove iz Zakona o javnoj nabavi koji vrijede na datum sklapanja (10 % za robe i usluge, 15 %
                        za radove; 30 % i 50 % za veće izmjene). Ugovori ispod 10.000 € i ugovori na temelju okvirnog
                        sporazuma se preskaču.
                    </li>
                    <li>
                        <strong>D – isplaćeno više od ugovorenog:</strong> isplaćeni iznos je više od 10 % i najmanje
                        5.000 € iznad ukupnog iznosa s PDV-om.
                    </li>
                    <li>
                        <strong>F – moguće cjepkanje nabave:</strong> isti naručitelj i dobavljač, ista CPV skupina, više
                        ugovora unutar 90 dana, svaki ispod praga jednostavne nabave koji je vrijedio na dan sklapanja, a
                        zajedno ga dosežu ili prelaze. Ugovori iz postupaka javne nabave i okvirnih sporazuma ne ulaze u skupinu; gorivo, energija i
                        komunalne usluge su samo informativni.
                    </li>
                    <li>
                        <strong>C – koncentracija dobavljača:</strong> u CPV odjeljku i zadnje tri godine s ugovorima
                        (najmanje 5 ugovora, 50.000 € i dvije godine) jedan dobavljač ima najmanje 70 % vrijednosti i barem
                        dva ugovora, a HHI je najmanje 5.000. Broj ponuda nije javno dostupan, pa se ne zna koliko se dobavljača natjecalo.
                    </li>
                    <li>
                        <strong>B – nedavno osnovana tvrtka:</strong> ugovor od najmanje 50.000 € sklopljen najviše 180 dana
                        nakon upisa dobavljača u sudski registar (Sudski registar, otvoreni API).
                    </li>
                    <li>
                        Pragovi pravila D, F, C i B su heuristike JavniRadara, osim zakonskih pragova izmjena i jednostavne
                        nabave iz Zakona o javnoj nabavi.
                    </li>
                    <li>
                        <strong>AI procjena – zahtijeva ljudsku provjeru:</strong> jezični model čita samo podatke ugovora i
                        rezultate pravila, a svaki razlog mora citirati polja i izvore iz tih podataka; inače se odbacuje.
                        Prikazuje se odvojeno od pravila, s modelom i datumom, i nikad se ne zbraja s njima.
                    </li>
                    <li>
                        Svaka oznaka ima tri odvojene dimenzije (kvaliteta podataka, veličina odstupanja, pouzdanost
                        dokaza) i nema zbirne ocjene. Kad brojevi upućuju na grešku pri unosu u registar, oznaka je
                        „informativno – moguća greška u unosu”. Takvi zapisi mogu pomoći i naručiteljima da poprave
                        objavljene podatke.
                    </li>
                    <li>Sintetički podaci za demonstraciju nikad ne ulaze u brojke stvarnih podataka.</li>
                </ul>
            </section>
        </>
    );
};
