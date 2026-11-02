package com.datathon.api.organization.dto;

import com.datathon.api.organization.Organization;
import com.datathon.api.organization.OrganizationType;

/**
 * A buyer or supplier as shown to users. The name of a craft (obrt) usually contains the owner's name, so it is
 * replaced (GDPR, docs/DATA_MODEL.md §8); the OIB and the link to the source stay.
 *
 * @param type database code of the organization type ({@code jls}, {@code zupanija}, {@code javno_tijelo}, {@code doo}, {@code obrt}...)
 */
public record OrganizationRef(Long id, String oib, String name, String type, boolean nameHidden, boolean synthetic) {

    public static final String HIDDEN_CRAFT_NAME = "Obrt (naziv skriven)";

    public static OrganizationRef from(Organization organization) {
        if (organization == null) {
            return null;
        }
        boolean isNameHidden = isNameHidden(organization);
        return new OrganizationRef(organization.getId(), organization.getOib(),
                isNameHidden ? HIDDEN_CRAFT_NAME : organization.getName(),
                organization.getOrgType() == null ? null : organization.getOrgType().code(),
                isNameHidden, organization.isSynthetic());
    }

    public static boolean isNameHidden(Organization organization) {
        return organization.getOrgType() == OrganizationType.CRAFT;
    }
}
