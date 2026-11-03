import type {OrganizationRef} from "@/types";
import styles from "./OrganizationName.module.css";

interface OrganizationNameProps {
    organization: OrganizationRef | null;
    showOib?: boolean;
}

/** A craft's name is hidden by the API (GDPR); the OIB stays so the record can still be checked in the source. */
export const OrganizationName = ({organization, showOib = false}: OrganizationNameProps) => {
    if (!organization) {
        return <span className={styles.muted}>nije objavljeno</span>;
    }
    const isNameHidden = organization.nameHidden || organization.type === "obrt";
    const isOibShown = showOib || isNameHidden;
    return (
        <span>
            <span className={isNameHidden ? styles.hidden : undefined}>{isNameHidden ? "Obrt (naziv skriven)" : organization.name}</span>
            {isOibShown && organization.oib && <span className={styles.oib}> · OIB {organization.oib}</span>}
        </span>
    );
};
