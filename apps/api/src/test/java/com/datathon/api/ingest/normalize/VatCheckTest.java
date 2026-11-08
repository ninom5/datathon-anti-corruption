package com.datathon.api.ingest.normalize;

import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.ingest.IssueCode;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/** Net and gross amounts from real registers (docs/DATA_SOURCES.md §1). */
class VatCheckTest {

    @Test
    void acceptsVatFromZeroToCarTax() {
        assertThat(VatCheck.check(eur("7000"), eur("7000"))).as("supplier outside the VAT system").isEmpty();
        assertThat(VatCheck.check(eur("10000"), eur("12500"))).isEmpty();
        assertThat(VatCheck.check(eur("17327.14"), eur("21859.92"))).as("car with special tax, Rovinj 2023").isEmpty();
        assertThat(VatCheck.check(null, eur("100"))).isEmpty();
    }

    @Test
    void flagsContradictingAmounts() {
        assertThat(VatCheck.check(eur("4211200"), eur("52640"))).contains(IssueCode.GROSS_BELOW_NET);
        assertThat(VatCheck.check(eur("9910.68"), eur("1238834.69"))).contains(IssueCode.VAT_RATIO_IMPLAUSIBLE);
    }

    private static BigDecimal eur(String value) {
        return new BigDecimal(value);
    }
}
