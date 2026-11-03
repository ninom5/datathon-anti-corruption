import styles from "./SyntheticBanner.module.css";

/** Full-width violet banner whenever synthetic records are on screen (docs/UI_UX.md §4, AGENTS.md). */
export const SyntheticBanner = () => (
    <div className={styles.banner} role="note">
        SINTETIČKI PODACI ZA DEMONSTRACIJU – izmišljene organizacije
    </div>
);
