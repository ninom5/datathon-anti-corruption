import {Link, useParams} from "wouter";
import {ApiError} from "@/api";
import {EmptyState, ErrorState, LoadingState, SyntheticBanner} from "@/components";
import {useBuyerProfile} from "@/hooks";
import {routes} from "@/routes";
import {BuyerHeader, BuyerIndicators, BuyerSources, BuyerSuppliers, CpvChart, YearChart} from "@/pages/BuyerPage";
import styles from "@/components/DataView.module.css";

export const BuyerPage = () => {
    const {oib = ""} = useParams<{oib: string}>();
    const query = useBuyerProfile(oib);
    if (!/^\d{11}$/.test(oib)) return <EmptyState title="Neispravna adresa naručitelja."><Link href={routes.CONTRACTS}>Istraži ugovore</Link></EmptyState>;
    if (query.error instanceof ApiError && query.error.status === 404) return <EmptyState title="Za ovog naručitelja nema objavljenih ugovora u podacima"><p>Provjerite OIB ili odaberite naručitelja u istraživaču. Nedostatak podataka ne znači da nabave nije bilo.</p><Link href={routes.CONTRACTS}>Istraži ugovore</Link></EmptyState>;
    if (query.error) return <ErrorState message={query.error.message} onRetry={() => query.refetch()}/>;
    if (query.isPending) return <LoadingState label="Učitavanje profila naručitelja…"/>;
    const profile = query.data;
    if (!profile.contracts) return <EmptyState title="Za ovog naručitelja nema objavljenih ugovora u podacima"><Link href={routes.CONTRACTS}>Istraži ugovore</Link></EmptyState>;
    return (
        <>
            {profile.synthetic && <div className={styles.bleed}><SyntheticBanner/></div>}
            <Link className={styles.back} href={routes.CONTRACTS}>← Istraživač ugovora</Link>
            <BuyerHeader profile={profile}/>
            <YearChart years={profile.byYear}/>
            <div className={styles.grid}><CpvChart rows={profile.byCpv} population={profile.municipality?.population2021}/><BuyerSuppliers profile={profile}/></div>
            <BuyerSources profile={profile}/>
            <BuyerIndicators key={oib} profile={profile}/>
        </>
    );
};
