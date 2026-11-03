import {ButtonLink, PageHeader} from "@/components";
import {comparisonPath, mapPath, routes} from "@/routes";
import type {BuyerProfile} from "@/types";
import {formatEurRounded, formatNumber} from "@/utils";
import styles from "@/components/DataView/DataView.module.css";

interface BuyerHeaderProps { profile: BuyerProfile }

export const BuyerHeader = ({profile}: BuyerHeaderProps) => {
    const county = profile.municipality?.county ?? profile.jpt?.county;
    const population = profile.municipality?.population2021;
    const params = new URLSearchParams({narucitelj: profile.buyer.oib ?? ""});
    if (profile.synthetic) params.set("sinteticki", "da");
    const hasMunicipalityLinks = profile.municipality && profile.buyer.oib;
    return (
        <>
            <PageHeader
                title={profile.buyer.name}
                description={`Profil naručitelja · OIB ${profile.buyer.oib}`}
                meta={
                    <ul className={styles.meta}>
                        <li>{county ?? "Županija nije objavljena"}</li>
                        <li>{population == null ? "Broj stanovnika nije objavljen" : `${formatNumber(population)} stanovnika (DZS 2021)`}</li>
                        <li>Usporedna skupina: {profile.municipality?.peerGroup ?? "nije određena"}</li>
                    </ul>
                }
                actions={
                    <>
                        {hasMunicipalityLinks && <ButtonLink href={mapPath(profile.buyer.oib ?? "")} variant="secondary">Prikaži na karti</ButtonLink>}
                        {hasMunicipalityLinks && <ButtonLink href={comparisonPath(profile.buyer.oib ?? "")} variant="secondary">Usporedi sa sličnim JLS</ButtonLink>}
                        <ButtonLink href={`${routes.CONTRACTS}?${params}`}>Istraži ugovore</ButtonLink>
                    </>
                }
            />
            <section className={styles.summaryBlock} aria-label="Sažetak">
                <p className={styles.summary}>
                    <strong className="tabular-nums">{formatNumber(profile.contracts)}</strong> ugovora
                    <span className={styles.dot}>·</span>
                    <strong className="tabular-nums">{formatEurRounded(profile.totalValue)}</strong> bez PDV-a
                </p>
                <p className={styles.note}>
                    Podaci iz registra: <strong>{profile.coverage.label}</strong> · <a href="#buyer-sources">Izvori i obuhvat</a>.
                    Zbroj vrijednosti objavljenih ugovora za navedeno razdoblje. Koristi se ukupni iznos sa izmjenama, a ako
                    nije objavljen, početni iznos; ovo nije zbroj isplata.
                </p>
            </section>
        </>
    );
};
