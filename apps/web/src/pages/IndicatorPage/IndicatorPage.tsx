import {Link, useParams} from "wouter";
import {ApiError} from "@/api";
import {BackLink, ButtonLink, EmptyState, ErrorState, IndicatorCard, LoadingState, PageHeader, SyntheticBanner} from "@/components";
import {useIndicator} from "@/hooks";
import {buyerPath, routes} from "@/routes";
import {ruleOptions} from "@/utils";
import {ConcentrationChart, indicatorContext, IndicatorContracts, SplittingChart} from "@/pages/IndicatorPage";
import styles from "@/components/DataView/DataView.module.css";

export const IndicatorPage = () => {
    const {id: rawId = ""} = useParams<{id: string}>();
    const id = /^\d+$/.test(rawId) ? Number(rawId) : NaN;
    const query = useIndicator(id);
    if (!Number.isSafeInteger(id) || id <= 0) return <EmptyState title="Neispravna adresa indikatora" actions={<ButtonLink href={routes.CONTRACTS} variant="secondary">Istraži ugovore</ButtonLink>}/>;
    if (query.error instanceof ApiError && query.error.status === 404) return <EmptyState title="Indikator ne postoji ili više nije aktivan" hint="Otvorite aktualni indikator iz istraživača ili profila naručitelja." actions={<ButtonLink href={routes.CONTRACTS} variant="secondary">Istraži ugovore</ButtonLink>}/>;
    if (query.error) return <ErrorState message={query.error.message} onRetry={() => query.refetch()}/>;
    if (query.isPending) return <LoadingState message="Učitavanje indikatora…"/>;
    const indicator = query.data;
    const context = indicatorContext(indicator.explanation);
    const rule = ruleOptions.find((option) => option.code === indicator.ruleCode);
    return (
        <>
            {indicator.synthetic && <SyntheticBanner/>}
            <BackLink href={routes.CONTRACTS}>Istraživač ugovora</BackLink>
            <PageHeader
                title={indicator.title}
                description={`Indikator ${rule?.short ?? ""} · ${indicator.id}`}
                meta={indicator.buyer && <>Naručitelj: {indicator.buyer.oib ? <Link href={buyerPath(indicator.buyer.oib)}>{indicator.buyer.name}</Link> : indicator.buyer.name}</>}
            />
            {context && <aside className={styles.context} aria-label="Kontekst iz izvora"><h2>Kontekst iz izvora</h2><p>{context}</p></aside>}
            <IndicatorCard indicator={indicator} showLink={false}/>
            {indicator.ruleCode === "F_SPLITTING" && <SplittingChart indicator={indicator}/>}
            {indicator.ruleCode === "C_CONCENTRATION" && <ConcentrationChart indicator={indicator}/>}
            <IndicatorContracts contracts={indicator.contracts}/>
        </>
    );
};
