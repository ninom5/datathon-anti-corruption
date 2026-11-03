import {Link} from "wouter";
import {OrganizationName} from "@/components";
import {contractPath} from "@/routes";
import type {IndicatorContract} from "@/types";
import {formatDate, formatEur} from "@/utils";
import styles from "@/components/DataView/DataView.module.css";

interface IndicatorContractsProps { contracts: IndicatorContract[] }

export const IndicatorContracts = ({contracts}: IndicatorContractsProps) => (
    <section className={styles.section}>
        <h2>Ugovori u indikatoru ({contracts.length})</h2>
        <p className={styles.note}>Otvorite ugovor za izvornu datoteku, redak registra, valutu i napomene naručitelja. Iznosi su u EUR bez PDV-a.</p>
        {contracts.length === 0 ? <p>Nema javno prikazivih ugovora u ovoj skupini.</p> : <div className={styles.tableWrap} tabIndex={0} role="region" aria-label="Ugovori u indikatoru">
            <table className={styles.table}><caption className="visually-hidden">Ugovori s datumima, dobavljačima i iznosima</caption>
                <thead><tr><th scope="col">Datum</th><th scope="col">Predmet / CPV</th><th scope="col">Dobavljač</th><th scope="col">Postupak</th><th scope="col" className={styles.numeric}>Početni iznos</th><th scope="col" className={styles.numeric}>Ukupni iznos</th></tr></thead>
                <tbody>{contracts.map((contract) => <tr key={contract.id}>
                    <td className={styles.date}>{formatDate(contract.dateSigned)}</td>
                    <td><Link href={contractPath(contract.id)}>{contract.subject}</Link><div className={styles.note}>CPV {contract.cpv ?? "nije objavljen"}</div></td>
                    <td><OrganizationName organization={contract.supplier} showOib/></td>
                    <td>{contract.procedureType ?? "nije objavljeno"}</td>
                    <td className={styles.numeric}>{formatEur(contract.initialValue)}</td><td className={styles.numeric}>{formatEur(contract.totalValue)}</td>
                </tr>)}</tbody>
            </table>
        </div>}
    </section>
);
