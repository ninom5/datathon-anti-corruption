import {MAP_CLASSES} from "./mapClasses.ts";
import styles from "./MapPage.module.css";

interface MapLegendProps {
    counts: Record<string, number>;
}

export const MapLegend = ({counts}: MapLegendProps) => (
    <figure className={styles.legend}>
        <figcaption className={styles.legendTitle}>Indikatori „vrijedi provjeriti” na 100 ugovora</figcaption>
        <ul className={styles.legendList}>
            {MAP_CLASSES.map((mapClass) => (
                <li key={mapClass.key} className={styles.legendItem}>
                    <svg width="18" height="14" aria-hidden="true">
                        <rect width="18" height="14" rx="2" className={`${styles.legendSwatch} ${styles[mapClass.key]}`}/>
                    </svg>
                    <span>{mapClass.label}</span>
                    <span className={styles.legendCount}>{counts[mapClass.key] ?? 0} JLS</span>
                </li>
            ))}
        </ul>
    </figure>
);
