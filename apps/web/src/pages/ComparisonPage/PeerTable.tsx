import {Link} from "wouter";
import {buyerPath} from "@/routes";
import type {PeerMember} from "@/types";
import {formatEur, formatEurRounded, formatNumber} from "@/utils";
import styles from "./ComparisonPage.module.css";

interface PeerTableProps {
    members: PeerMember[];
    isAllCategories: boolean;
}

export const PeerTable = ({members, isAllCategories}: PeerTableProps) => (
    <div className={styles.tableScroll}>
        <table className={styles.table}>
            <caption className={styles.caption}>Sve JLS iz skupine s ugovorima u odabranoj godini (n = {members.length})</caption>
            <thead>
            <tr>
                <th scope="col">#</th>
                <th scope="col">JLS</th>
                <th scope="col" className={styles.number}>Stanovnika</th>
                <th scope="col" className={styles.number}>{isAllCategories ? "Ugovora" : "Ugovora u kategoriji"}</th>
                <th scope="col" className={styles.number}>Ukupno</th>
                <th scope="col" className={styles.number}>Po stanovniku</th>
            </tr>
            </thead>
            <tbody>
            {members.map((member, index) => (
                <tr key={member.oib} className={member.selected ? styles.selectedRow : undefined}
                    aria-current={member.selected ? "true" : undefined}>
                    <td>{index + 1}.</td>
                    <th scope="row">
                        <Link href={buyerPath(member.oib)}>{member.name}</Link>
                    </th>
                    <td className={styles.number}>
                        {member.population2021 === null ? "nije objavljeno" : formatNumber(member.population2021)}
                    </td>
                    <td className={styles.number}>
                        {formatNumber(member.contracts)}
                        {!isAllCategories && <span className={styles.muted}> / {formatNumber(member.contractsInYear)}</span>}
                    </td>
                    <td className={styles.number}>{formatEurRounded(member.totalValue)}</td>
                    <td className={styles.number}>{formatEur(member.valuePerCapita)}</td>
                </tr>
            ))}
            </tbody>
        </table>
    </div>
);
