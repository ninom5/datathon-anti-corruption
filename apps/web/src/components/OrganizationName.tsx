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
    const isOibShown = showOib || organization.nameHidden;
    return (
        <span>
            <span className={organization.nameHidden ? styles.hidden : undefined}>{organization.name}</span>
            {isOibShown && organization.oib && <span className={styles.oib}> · OIB {organization.oib}</span>}
        </span>
    );
};
