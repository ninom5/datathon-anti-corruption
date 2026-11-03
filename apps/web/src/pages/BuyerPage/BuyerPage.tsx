import {useParams} from "wouter";
import {ApiError} from "@/api";
import {BackLink, ButtonLink, EmptyState, ErrorState, LoadingState, SyntheticBanner} from "@/components";
import {useBuyerProfile} from "@/hooks";
import {routes} from "@/routes";
import {BuyerHeader, BuyerIndicators, BuyerSources, BuyerSuppliers, CpvChart, YearChart} from "@/pages/BuyerPage";
import styles from "@/components/DataView/DataView.module.css";

export const BuyerPage = () => {
    const {oib = ""} = useParams<{oib: string}>();
    const query = useBuyerProfile(oib);
    if (!/^\d{11}$/.test(oib)) return <EmptyState title="Neispravna adresa naručitelja" actions={<ButtonLink href={routes.CONTRACTS} variant="secondary">Istraži ugovore</ButtonLink>}/>;
    if (query.error instanceof ApiError && query.error.status === 404) return <EmptyState title="Za ovog naručitelja nema objavljenih ugovora u podacima" hint="Provjerite OIB ili odaberite naručitelja u istraživaču. Nedostatak podataka ne znači da nabave nije bilo." actions={<ButtonLink href={routes.CONTRACTS} variant="secondary">Istraži ugovore</ButtonLink>}/>;
    if (query.error) return <ErrorState message={query.error.message} onRetry={() => query.refetch()}/>;
    if (query.isPending) return <LoadingState message="Učitavanje profila naručitelja…"/>;
    const profile = query.data;
    if (!profile.contracts) return <EmptyState title="Za ovog naručitelja nema objavljenih ugovora u podacima" actions={<ButtonLink href={routes.CONTRACTS} variant="secondary">Istraži ugovore</ButtonLink>}/>;
    return (
        <>
            {profile.synthetic && <SyntheticBanner/>}
            <BackLink href={routes.CONTRACTS}>Istraživač ugovora</BackLink>
            <BuyerHeader profile={profile}/>
            <YearChart years={profile.byYear}/>
            <div className={styles.grid}><CpvChart rows={profile.byCpv} population={profile.municipality?.population2021}/><BuyerSuppliers profile={profile}/></div>
            <BuyerSources profile={profile}/>
            <BuyerIndicators key={oib} profile={profile}/>
        </>
    );
};
