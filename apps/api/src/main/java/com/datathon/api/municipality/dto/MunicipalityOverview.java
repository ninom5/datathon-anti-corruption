package com.datathon.api.municipality.dto;

import java.math.BigDecimal;

/**
 * One JLS on the map. {@code contracts == 0} means it publishes no machine-readable contract register on
 * data.gov.hr (or none that we import), which the map shows as its own category, never as zero indicators.
 *
 * @param kind                    {@code grad}, {@code opcina} or {@code grad_zagreb}
 * @param worthChecking           active indicators marked "vrijedi provjeriti"
 * @param informative             active indicators shown for information only (e.g. likely data entry errors)
 * @param worthCheckingPer100     {@code worthChecking} per 100 contracts, one decimal; null without contracts
 */
public record MunicipalityOverview(
        String oib,
        String name,
        String kind,
        String county,
        Integer population2021,
        String peerGroup,
        long contracts,
        Integer firstYear,
        Integer lastYear,
        long worthChecking,
        long informative,
        BigDecimal worthCheckingPer100) {
}
