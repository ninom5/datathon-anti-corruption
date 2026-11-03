import {Link} from "wouter";
import {comparisonPath, mapPath, routes} from "@/routes";
import type {BuyerProfile} from "@/types";
import {formatEurRounded, formatNumber} from "@/utils";
import styles from "@/components/DataView.module.css";

interface BuyerHeaderProps { profile: BuyerProfile }

export const BuyerHeader = ({profile}: BuyerHeaderProps) => {
    const county = profile.municipality?.county ?? profile.jpt?.county;
    const population = profile.municipality?.population2021;
    const params = new URLSearchParams({narucitelj: profile.buyer.oib ?? ""});
    if (profile.synthetic) params.set("sinteticki", "da");
    return (
        <header className={styles.header}>
            <p className={styles.eyebrow}>Profil naručitelja · OIB {profile.buyer.oib}</p>
            <h1>{profile.buyer.name}</h1>
            <ul className={styles.meta}>
                <li>{county ?? "Županija nije objavljena"}</li>
                <li>{population == null ? "Broj stanovnika nije objavljen" : `${formatNumber(population)} stanovnika (DZS 2021)`}</li>
                <li>Usporedna skupina: {profile.municipality?.peerGroup ?? "nije određena"}</li>
            </ul>
            <p>Podaci iz registra: <strong>{profile.coverage.label}</strong> · <a href="#buyer-sources">ⓘ Izvori i obuhvat</a></p>
            <p className={styles.summary}><strong>{formatNumber(profile.contracts)}</strong> ugovora · <strong>{formatEurRounded(profile.totalValue)}</strong> bez PDV-a</p>
            <p className={styles.note}>Zbroj vrijednosti objavljenih ugovora za navedeno razdoblje. Koristi se ukupni iznos sa izmjenama, a ako nije objavljen, početni iznos; ovo nije zbroj isplata.</p>
            <Link href={`${routes.CONTRACTS}?${params}`}>Istraži ugovore ovog naručitelja →</Link>
            {profile.municipality && profile.buyer.oib && (
                <p>
                    <Link href={comparisonPath(profile.buyer.oib)}>Usporedi sa sličnim JLS</Link>
                    {" · "}
                    <Link href={mapPath(profile.buyer.oib)}>Prikaži na karti</Link>
                </p>
            )}
        </header>
    );
};
