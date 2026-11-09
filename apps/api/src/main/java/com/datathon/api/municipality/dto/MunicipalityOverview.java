package com.datathon.api.municipality.dto;

import java.math.BigDecimal;

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
