package com.datathon.api.organization.dto;

import com.datathon.api.organization.Organization;
import com.datathon.api.organization.OrganizationType;
import java.util.List;
import java.util.Locale;

public record OrganizationRef(Long id, String oib, String name, String type, boolean nameHidden, boolean synthetic) {

    public static final String HIDDEN_CRAFT_NAME = "Obrt (naziv skriven)";
    public static final String HIDDEN_OTHER_NAME = "Naziv skriven (moguć obrt ili osoba)";

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
