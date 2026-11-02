import {formatNumber} from "@/utils";
import styles from "./ContractsPage.module.css";

interface PaginationProps {
    page: number;
    size: number;
    totalElements: number;
    onPageChange: (page: number) => void;
}

export const Pagination = ({page, size, totalElements, onPageChange}: PaginationProps) => {
    const pageCount = Math.max(1, Math.ceil(totalElements / size));
    const first = totalElements === 0 ? 0 : page * size + 1;
    const last = Math.min(totalElements, (page + 1) * size);
    return (
        <nav className={styles.pagination} aria-label="Stranice rezultata">
            <span className="tabular">
                {formatNumber(first)}–{formatNumber(last)} od {formatNumber(totalElements)}
            </span>
            <span className={styles.pageButtons}>
                <button type="button" className={styles.smallButton} disabled={page === 0} onClick={() => onPageChange(page - 1)}>
                    Prethodna
                </button>
                <span className="tabular">{page + 1} / {pageCount}</span>
                <button type="button" className={styles.smallButton} disabled={page + 1 >= pageCount} onClick={() => onPageChange(page + 1)}>
                    Sljedeća
                </button>
            </span>
        </nav>
    );
};
