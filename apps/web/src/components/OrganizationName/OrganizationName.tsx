import type {OrganizationRef} from "@/types";
import styles from "./OrganizationName.module.css";

interface OrganizationNameProps {
    organization: OrganizationRef | null;
    showOib?: boolean;
}

/**
 * Names that may be a person's (crafts, unclassified suppliers) are replaced by the API with a placeholder (GDPR);
 * the OIB stays so the record can still be checked in the source.
 */
export const OrganizationName = ({organization, showOib = false}: OrganizationNameProps) => {
    if (!organization) {
        return <span className={styles.muted}>nije objavljeno</span>;
    }
    const isNameHidden = organization.nameHidden || organization.type === "obrt";
    const isOibShown = showOib || isNameHidden;
    return (
        <span>
            <span className={isNameHidden ? styles.hidden : undefined}>{isNameHidden && organization.type === "obrt" && !organization.nameHidden ? "Obrt (naziv skriven)" : organization.name}</span>
            {isOibShown && organization.oib && <span className={styles.oib}> · OIB {organization.oib}</span>}
        </span>
    );
};
