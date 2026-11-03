package com.datathon.api.indicator.dto;

import java.util.List;

/** Rule codes a client may filter on; kept as constants so validation annotations can use them. */
public final class RuleCodes {

    public static final String PATTERN = "D_VALUE_INCREASE|D_OVERPAYMENT|F_SPLITTING|C_CONCENTRATION";
    public static final String MESSAGE = "dopuštene vrijednosti su D_VALUE_INCREASE, D_OVERPAYMENT, F_SPLITTING i C_CONCENTRATION";
    public static final List<String> ALL = List.of("D_VALUE_INCREASE", "D_OVERPAYMENT", "F_SPLITTING", "C_CONCENTRATION");

    private RuleCodes() {
    }
}
