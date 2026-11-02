import {Link, useParams} from "wouter";
import {EmptyState, ErrorState, LoadingState, OrganizationName, SyntheticBanner} from "@/components";
import {isClientError} from "@/api";
import {useContract} from "@/hooks";
import {routes} from "@/routes";
import type {ContractDetail} from "@/types";
import {buyerTypeLabels, formatDate, qualityFlagLabel, subjectTypeLabels} from "@/utils";
import {IndicatorCard} from "./IndicatorCard.tsx";
import {ProvenanceBlock} from "./ProvenanceBlock.tsx";
import {ValuesTable} from "./ValuesTable.tsx";
import styles from "./ContractPage.module.css";

const ContractFacts = ({contract}: {contract: ContractDetail}) => (
    <section className={styles.block} aria-labelledby="facts-title">
        <h2 id="facts-title">Podaci iz registra</h2>
        <dl className={styles.facts}>
            <dt>Vrsta postupka</dt>
            <dd>{contract.procedureType ?? "nije objavljeno"}</dd>
            <dt>Vrsta predmeta</dt>
            <dd>{contract.subjectType ? subjectTypeLabels[contract.subjectType] : "nije objavljeno"}</dd>
            <dt>CPV</dt>
            <dd>{contract.cpv ? `${contract.cpv.code} ${contract.cpv.label ?? ""}` : "nije objavljeno"}</dd>
            <dt>Broj ugovora</dt>
            <dd>{contract.contractNumber ?? "nije objavljeno"}</dd>
            <dt>Trajanje</dt>
            <dd>{contract.durationText ?? "nije objavljeno"}</dd>
            <dt>Datum izvršenja</dt>
            <dd className="tabular">{formatDate(contract.completedOn)}</dd>
            <dt>Okvirni sporazum</dt>
            <dd>{contract.frameworkBased ? "ugovor na temelju okvirnog sporazuma" : "ne"}</dd>
            {contract.note && (
                <>
                    <dt>Napomena</dt>
                    <dd>{contract.note}</dd>
                </>
            )}
        </dl>
        {contract.qualityFlags.length > 0 && (
            <div className={styles.flags}>
                <h3>Upozorenja pri uvozu</h3>
                <ul>{contract.qualityFlags.map((flag) => <li key={flag}>{qualityFlagLabel(flag)}</li>)}</ul>
            </div>
        )}
    </section>
);

const Indicators = ({contract}: {contract: ContractDetail}) => (
    <section className={styles.block} aria-labelledby="indicators-title">
        <h2 id="indicators-title">Oznake pravila</h2>
        {contract.indicators.length === 0 && (
            <p className={styles.muted}>
                Pravila nisu označila ništa u ovom ugovoru. To ne znači da je ugovor provjeren, nego samo da objavljeni
                iznosi ne prelaze pragove pravila.
            </p>
        )}
        {contract.indicators.map((indicator) => <IndicatorCard key={indicator.id} indicator={indicator}/>)}
        {contract.supplier?.nameHidden && (
            <p className={styles.muted}>
                AI procjena se ne radi za ugovore s obrtima, jer naziv obrta najčešće sadrži ime vlasnika.
            </p>
        )}
    </section>
);

const ContractView = ({contract}: {contract: ContractDetail}) => {
    const buyerType = contract.buyer.type ? buyerTypeLabels[contract.buyer.type] : undefined;
    return (
        <>
            {contract.synthetic && <div className={styles.bleed}><SyntheticBanner/></div>}
            <Link href={routes.CONTRACTS} className={styles.back}>← Istraživač ugovora</Link>
            <header className={styles.header}>
                <h1>{contract.subject}</h1>
                <p className={styles.parties}>
                    <span>{contract.buyer.name}</span>
                    {buyerType && <span className={styles.muted}> ({buyerType})</span>}
                    <span className={styles.arrow} aria-label="ugovor s">→</span>
                    <OrganizationName organization={contract.supplier} showOib/>
                </p>
                <p className={styles.muted}>
                    Sklopljen <span className="tabular">{formatDate(contract.dateSigned)}</span>
                    {contract.yearIncomplete && <> · {contract.year}. je nepotpuna godina u podacima</>}
                </p>
            </header>
            <div className={styles.grid}>
                <div className={styles.mainColumn}>
                    <Indicators contract={contract}/>
                    <ContractFacts contract={contract}/>
                </div>
                <div className={styles.sideColumn}>
                    <ValuesTable values={contract.values}/>
                    <ProvenanceBlock contract={contract}/>
                </div>
            </div>
        </>
    );
};

export const ContractPage = () => {
    const params = useParams<{id: string}>();
    const id = Number(params.id);
    const contract = useContract(id);

    if (!Number.isInteger(id) || id <= 0) {
        return <EmptyState title="Neispravna adresa ugovora."><Link href={routes.CONTRACTS}>Istraži ugovore</Link></EmptyState>;
    }
    if (isClientError(contract.error)) {
        return (
            <EmptyState title="Ugovor ne postoji ili nije javno prikazan.">
                <p>Ugovori s fizičkim osobama se ne prikazuju radi zaštite osobnih podataka.</p>
                <Link href={routes.CONTRACTS}>Istraži ugovore</Link>
            </EmptyState>
        );
    }
    if (contract.error) {
        return <ErrorState message={contract.error.message} onRetry={() => contract.refetch()}/>;
    }
    if (contract.isPending) {
        return <LoadingState label="Učitavanje ugovora…"/>;
    }
    return <ContractView contract={contract.data}/>;
};
