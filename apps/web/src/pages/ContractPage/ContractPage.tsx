import {Link, useParams} from "wouter";
import {BackLink, ButtonLink, EmptyState, ErrorState, IndicatorCard, LoadingState, OrganizationName, PageHeader, SyntheticBanner} from "@/components";
import {isClientError} from "@/api";
import {useContract} from "@/hooks";
import {buyerPath, routes} from "@/routes";
import type {ContractDetail} from "@/types";
import {buyerTypeLabels, formatDate, qualityFlagLabel, subjectTypeLabels} from "@/utils";
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
            <dd className="tabular-nums">{formatDate(contract.completedOn)}</dd>
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
        {contract.indicators.map((indicator) => <IndicatorCard key={indicator.id} indicator={indicator} inContract/>)}
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
            {contract.synthetic && <SyntheticBanner/>}
            <BackLink href={routes.CONTRACTS}>Istraživač ugovora</BackLink>
            <PageHeader
                title={contract.subject}
                meta={
                    <>
                        <p className={styles.parties}>
                            {contract.buyer.oib ? <Link href={buyerPath(contract.buyer.oib)}>{contract.buyer.name}</Link> : <span>{contract.buyer.name}</span>}
                            {buyerType && <span className={styles.muted}> ({buyerType})</span>}
                            <span className={styles.arrow} aria-label="ugovor s">→</span>
                            <OrganizationName organization={contract.supplier} showOib/>
                        </p>
                        <p>
                            Sklopljen <span className="tabular-nums">{formatDate(contract.dateSigned)}</span>
                            {contract.yearIncomplete && <> · {contract.year}. je nepotpuna godina u podacima</>}
                        </p>
                    </>
                }
            />
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
        return <EmptyState title="Neispravna adresa ugovora" actions={<ButtonLink href={routes.CONTRACTS} variant="secondary">Istraži ugovore</ButtonLink>}/>;
    }
    if (isClientError(contract.error)) {
        return (
            <EmptyState
                icon="document"
                title="Ugovor ne postoji ili nije javno prikazan"
                hint="Ugovori s fizičkim osobama se ne prikazuju radi zaštite osobnih podataka."
                actions={<ButtonLink href={routes.CONTRACTS} variant="secondary">Istraži ugovore</ButtonLink>}
            />
        );
    }
    if (contract.error) {
        return <ErrorState message={contract.error.message} onRetry={() => contract.refetch()}/>;
    }
    if (contract.isPending) {
        return <LoadingState message="Učitavanje ugovora…"/>;
    }
    return <ContractView contract={contract.data}/>;
};
