import type {BuyerProfile} from "@/types";
import {CPV_SOURCE, formatNumber} from "@/utils";
import styles from "@/components/DataView.module.css";

interface BuyerSourcesProps { profile: BuyerProfile }

export const BuyerSources = ({profile}: BuyerSourcesProps) => (
    <section id="buyer-sources" className={styles.section}>
        <h2>Izvori i obuhvat</h2>
        <p className={styles.note}>Svi grafovi i popisi izračunati su iz ovih registara. Godina bez objavljenih ugovora nije dokaz da nabave nije bilo. Detalj svakog ugovora vodi do datoteke i retka izvora.</p>
        <ul className={styles.sourceList}>
            {profile.sources.map((source) => (
                <li key={`${source.landingUrl}-${source.formatClass}-${source.datasetTitle}`}>
                    {source.landingUrl ? <a href={source.landingUrl} target="_blank" rel="noreferrer">{source.datasetTitle}</a> : source.datasetTitle}
                    {" · "}format {source.formatClass ?? "nije objavljen"} · {formatNumber(source.files)} datoteka · {formatNumber(source.contracts)} ugovora
                </li>
            ))}
            {profile.municipality && <li>Stanovništvo: <a href="https://data.gov.hr/dataset/statistika-u-nizu-popis-stanovnistva-kucanstava-i-stanova-2021" target="_blank" rel="noreferrer">DZS, Popis 2021.</a> Usporednu skupinu iz tih podataka određuje JavniRadar.</li>}
            {profile.jpt && <li>Vrsta naručitelja i županija: <a href="https://data.gov.hr/dataset/popis-javnopravnih-tijela" target="_blank" rel="noreferrer">Registar javnopravnih tijela (JPT)</a>, oznaka {profile.jpt.code}.</li>}
            <li>Nazivi CPV odjeljaka skraćeni su radi čitljivosti: <a href={CPV_SOURCE} target="_blank" rel="noreferrer">CPV 2008, Uredba 213/2008</a>.</li>
        </ul>
    </section>
);
