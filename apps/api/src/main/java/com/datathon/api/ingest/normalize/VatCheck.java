package com.datathon.api.ingest.normalize;

import com.datathon.api.ingest.IssueCode;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Compares an amount without VAT with the same amount with VAT. The register cannot tell which of the two is mistyped,
 * so nothing is corrected; the row gets a quality flag that lowers data quality in the rules and is shown in the UI.
 */
public final class VatCheck {

    /** VAT is at most 25 %; the special tax on cars can add a few percent, so 1,40 leaves room for real cases. */
    private static final BigDecimal MAX_GROSS_TO_NET = new BigDecimal("1.40");

    private VatCheck() {
    }

    public static Optional<String> check(BigDecimal net, BigDecimal gross) {
        if (net == null || gross == null || net.signum() <= 0 || gross.signum() <= 0) {
            return Optional.empty();
        }
        if (gross.compareTo(net) < 0) {
            return Optional.of(IssueCode.GROSS_BELOW_NET);
        }
        if (gross.compareTo(net.multiply(MAX_GROSS_TO_NET)) > 0) {
            return Optional.of(IssueCode.VAT_RATIO_IMPLAUSIBLE);
        }
        return Optional.empty();
    }

    /** Adds the flag of each net/gross pair once. */
    public static void addFlags(List<String> flags, BigDecimal[]... pairs) {
        for (BigDecimal[] pair : pairs) {
            check(pair[0], pair[1]).filter(flag -> !flags.contains(flag)).ifPresent(flags::add);
        }
    }
}
