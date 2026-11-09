package com.datathon.api.organization.dto;

import com.datathon.api.organization.OrganizationType;

public record BuyerOption(Long id, String oib, String name, String type, long contracts, boolean synthetic) {

    public BuyerOption(Long id, String oib, String name, OrganizationType type, long contracts, boolean synthetic) {
        this(id, oib, name, type == null ? null : type.code(), contracts, synthetic);
    }
}
