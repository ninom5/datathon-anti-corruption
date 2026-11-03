import {Button, Icon} from "@/components";
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
            <span className={`tabular-nums ${styles.pageStatus}`}>
                {formatNumber(first)}–{formatNumber(last)} od {formatNumber(totalElements)}
            </span>
            <span className={styles.pageButtons}>
                <Button variant="secondary" disabled={page === 0} onClick={() => onPageChange(page - 1)}>
                    <Icon name="chevronLeft"/>
                    Prethodna
                </Button>
                <span className={`tabular-nums ${styles.pageStatus}`}>Stranica {page + 1} od {pageCount}</span>
                <Button variant="secondary" disabled={page + 1 >= pageCount} onClick={() => onPageChange(page + 1)}>
                    Sljedeća
                    <Icon name="chevronRight"/>
                </Button>
            </span>
        </nav>
    );
};
