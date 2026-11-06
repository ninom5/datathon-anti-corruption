package com.datathon.api.organization.dto;

import com.datathon.api.organization.Organization;
import com.datathon.api.organization.OrganizationType;
import java.util.List;
import java.util.Locale;

/**
 * A buyer or supplier as shown to users. The name of a craft (obrt) usually contains the owner's name, so it is
 * replaced (GDPR, docs/DATA_MODEL.md §8); the OIB and the link to the source stay. Suppliers the classifier left as
 * "ostalo" or "nepoznato" also hold crafts with personal names ("TERMODOR, DRAGUTIN KRIZMANIĆ", "Dr. … …"), so their
 * name is shown only when it is clearly an institution or the organization is in the JPT register (decision
 * 2026-11-05): hiding a company by mistake is better than showing a person.
 *
 * @param type database code of the organization type ({@code jls}, {@code zupanija}, {@code javno_tijelo}, {@code doo}, {@code obrt}...)
 */
public record OrganizationRef(Long id, String oib, String name, String type, boolean nameHidden, boolean synthetic) {

    public static final String HIDDEN_CRAFT_NAME = "Obrt (naziv skriven)";
    public static final String HIDDEN_OTHER_NAME = "Naziv skriven (moguć obrt ili osoba)";

    /** Words of names that are institutions, never a person ("Zavod za javno zdravstvo …", "Gradski muzej …"). */
    private static final List<String> INSTITUTION_WORDS = List.of("zavod", "ustanova", "zadruga", "muzej", "kazalište",
            "kazaliste", "institut", "dom zdravlja", "agencija", "klub", "društvo", "drustvo", "zajednica", "kaznionica",
            "fakultet", "škola", "skola", "centar", "savez", "fond", "hrvatsk", "županij", "zupanij", "grad", "općin",
            "opcin", "javna", "sveučili", "sveucili", "knjižnic", "knjiznic", "vrtić", "vrtic", "kovnica", "ekomuzej",
            "bolnica", "dvd", "vatrogas", "lovačk", "lovack", "udruga");

    public static OrganizationRef from(Organization organization) {
        if (organization == null) {
            return null;
        }
        boolean isNameHidden = isNameHidden(organization);
        return new OrganizationRef(organization.getId(), organization.getOib(),
                displayName(organization),
                organization.getOrgType() == null ? null : organization.getOrgType().code(),
                isNameHidden, organization.isSynthetic());
    }

    public static boolean isNameHidden(Organization organization) {
        OrganizationType type = organization.getOrgType();
        if (type == OrganizationType.CRAFT || type == OrganizationType.NATURAL_PERSON) {
            return true;
        }
        boolean isUnclassified = type == null || type == OrganizationType.OTHER || type == OrganizationType.UNKNOWN;
        return isUnclassified && organization.getJptCode() == null && !looksLikeInstitution(organization.getName());
    }

    /** The name users may see: the real one, or a placeholder that says why it is hidden. */
    public static String displayName(Organization organization) {
        if (!isNameHidden(organization)) {
            return organization.getName();
        }
        return organization.getOrgType() == OrganizationType.CRAFT ? HIDDEN_CRAFT_NAME : HIDDEN_OTHER_NAME;
    }

    private static boolean looksLikeInstitution(String name) {
        String lower = name == null ? "" : name.toLowerCase(Locale.ROOT);
        return INSTITUTION_WORDS.stream().anyMatch(lower::contains);
    }
}
