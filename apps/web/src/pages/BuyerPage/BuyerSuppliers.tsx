import {OrganizationName} from "@/components";
import type {BuyerProfile} from "@/types";
import {formatEur, formatNumber} from "@/utils";
import styles from "@/components/DataView.module.css";

interface BuyerSuppliersProps { profile: BuyerProfile }

export const BuyerSuppliers = ({profile}: BuyerSuppliersProps) => (
    <section className={styles.section}>
        <h2>Najveći dobavljači</h2>
        <p className={styles.note}>Najviše 10 dobavljača po vrijednosti u cijelom razdoblju. <a href="#buyer-sources">Izvor: registri ugovora</a>.</p>
        <p>{formatNumber(profile.craftContracts)} ugovora s obrtima uključeno je u brojke, ali bez naziva. Obrti nisu na ovom popisu.</p>
        {profile.topSuppliers.length === 0 ? <p>Nema dobavljača za prikaz na ovom popisu.</p> : <div className={styles.tableWrap} tabIndex={0} role="region" aria-label="Najveći dobavljači">
            <table className={styles.table}><caption className="visually-hidden">Najveći dobavljači prema vrijednosti ugovora</caption>
                <thead><tr><th scope="col">Dobavljač</th><th scope="col" className={styles.numeric}>Ugovori</th><th scope="col" className={styles.numeric}>EUR bez PDV-a</th></tr></thead>
                <tbody>{profile.topSuppliers.map((row) => <tr key={row.supplier.id}><td><OrganizationName organization={row.supplier} showOib/></td><td className={styles.numeric}>{formatNumber(row.contracts)}</td><td className={styles.numeric}>{formatEur(row.totalValue)}</td></tr>)}</tbody>
            </table>
        </div>}
    </section>
);
