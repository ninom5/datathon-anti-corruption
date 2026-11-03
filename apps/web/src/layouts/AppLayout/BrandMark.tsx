import styles from "./AppLayout.module.css";

// Radar rings with one amber point: something in the data worth checking. Same drawing as public/favicon.svg.
export const BrandMark = () => {
    return (
        <span className={styles.brandMark} aria-hidden="true">
            <svg viewBox="0 0 32 32" width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2.5">
                <circle cx="16" cy="16" r="11"/>
                <circle cx="16" cy="16" r="4.5"/>
                <circle cx="23.5" cy="9" r="3" fill="var(--map-1)" stroke="none"/>
            </svg>
        </span>
    );
};
