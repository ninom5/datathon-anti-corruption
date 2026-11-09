import {Link} from "wouter";
import {IndicatorTag, OrganizationName} from "@/components";
import {buyerPath, contractPath} from "@/routes";
import type {ContractListItem} from "@/types";
import {formatDate, formatEurRounded} from "@/utils";
import styles from "./ContractsPage.module.css";

interface ContractTableProps {
    contracts: ContractListItem[];
}

const displayedValue = (contract: ContractListItem) => contract.totalValue ?? contract.initialValue;

export const ContractTable = ({contracts}: ContractTableProps) => (
    <div className={styles.tableWrap}>
        <table className={styles.table}>
            <caption className="visually-hidden">Ugovori za odabrane filtre</caption>
            <thead>
            <tr>
                <th scope="col">Datum</th>
                <th scope="col">Naručitelj</th>
                <th scope="col">Dobavljač</th>
                <th scope="col">Predmet</th>
                <th scope="col" className={styles.numeric}>Iznos bez PDV-a</th>
                <th scope="col">Oznake</th>
            </tr>
            </thead>
            <tbody>
            {contracts.map((contract) => (
                <tr key={contract.id} className={contract.synthetic ? styles.syntheticRow : undefined}>
                    <td data-label="Datum" className={`${styles.date} tabular-nums`}>
                        {formatDate(contract.dateSigned)}
                        {contract.yearIncomplete && <span className={styles.muted}> *</span>}
                    </td>
                    <td data-label="Naručitelj">{contract.buyer.oib ? <Link href={buyerPath(contract.buyer.oib)}>{contract.buyer.name}</Link> : contract.buyer.name}</td>
                    <td data-label="Dobavljač"><OrganizationName organization={contract.supplier}/></td>
                    <td data-label="Predmet">
                        <Link href={contractPath(contract.id)} className={styles.subjectLink}>{contract.subject}</Link>
                        {contract.synthetic && <span className={styles.syntheticTag}>sintetički</span>}
                    </td>
                    <td data-label="Iznos" className={`${styles.numeric} tabular-nums`}>
                        {formatEurRounded(displayedValue(contract))}
                        {contract.amountDoubtful && (
                            <span className={styles.doubtfulAmount} title="Iznosi u registru se međusobno ne slažu (npr. iznos s PDV-om i bez PDV-a). Razlog je na stranici ugovora.">
                                iznosi u registru se ne slažu
                            </span>
                        )}
                    </td>
                    <td data-label="Oznake">
                        <span className={styles.tags}>
                            {contract.indicators.map((indicator) => <IndicatorTag key={indicator.id} indicator={indicator}/>)}
                        </span>
                    </td>
                </tr>
            ))}
            </tbody>
        </table>
    </div>
);
