import styles from "./DataView.module.css";

interface HorizontalBarsProps {
    rows: {id: string; label: string; value: number; formatted: string}[];
    summary: string;
    maximum?: number;
}

export const HorizontalBars = ({rows, summary, maximum}: HorizontalBarsProps) => {
    const scale = maximum ?? Math.max(1, ...rows.map((row) => row.value));
    return (
        <div role="img" aria-label={summary} className={styles.bars}>
            {rows.map((row) => (
                <div key={row.id} className={styles.barRow}>
                    <div className={styles.barLabel}><span>{row.label}</span><strong className="tabular">{row.formatted}</strong></div>
                    <svg viewBox="0 0 1000 12" preserveAspectRatio="none" aria-hidden="true">
                        <title>{row.label}: {row.formatted}</title>
                        <rect width="1000" height="12" className={styles.track}/>
                        <rect width={Math.max(0, Math.min(1000, row.value / scale * 1000))} height="12" className={styles.signalFill}/>
                    </svg>
                </div>
            ))}
        </div>
    );
};
