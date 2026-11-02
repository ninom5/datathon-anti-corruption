package com.datathon.api.organization.dto;

import com.datathon.api.organization.OrganizationType;

/**
 * A buyer for the explorer filter.
 *
 * @param type {@code jls} (municipality or city), {@code zupanija} (county) or {@code javno_tijelo} (other public
 *             body, e.g. a kindergarten); counties and other bodies are not JLS
 */
public record BuyerOption(Long id, String oib, String name, String type, long contracts, boolean synthetic) {

    public BuyerOption(Long id, String oib, String name, OrganizationType type, long contracts, boolean synthetic) {
        this(id, oib, name, type == null ? null : type.code(), contracts, synthetic);
    }
}
