package com.datathon.api.rules;

import com.datathon.api.contract.Contract;
import com.datathon.api.indicator.DataQuality;
import com.datathon.api.source.RawRecord;
import com.datathon.api.source.SourceFile;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Shared pieces of contract rules: data quality, evidence links and Croatian number formatting. */
final class RuleSupport {

    private static final Pattern LOCATOR = Pattern.compile("sheet=(\\d+);row=(\\d+)");
    /** "Ugovorena cijena je po jediničnim količinama iz troškovnika" (real note, Općina Mikleuš 2025). */
    private static final Pattern UNIT_PRICE_NOTE = Pattern.compile("jedinič|jedinic", Pattern.CASE_INSENSITIVE);
    private static final Pattern FRAMEWORK_SUBJECT = Pattern.compile("okvirn", Pattern.CASE_INSENSITIVE);

    private RuleSupport() {
    }

    /**
     * docs/ANOMALY_DETECTION.md §2: a parser-read class A row is "visoka"; any quality flag from ingest or an input
     * the rule had to derive makes it "srednja". "niska" is decided by the rule (implausible numbers).
     */
    static DataQuality baseQuality(Contract contract, boolean hasDerivedInput) {
        return contract.getQualityFlags().isEmpty() && !hasDerivedInput ? DataQuality.HIGH : DataQuality.MEDIUM;
    }

    /** Framework agreements have indicative quantities, so growth and overpayment say little (ANOMALY_DETECTION.md §D). */
    static boolean isFrameworkContract(Contract contract) {
        return contract.isFrameworkBased() || (contract.getSubject() != null && FRAMEWORK_SUBJECT.matcher(contract.getSubject()).find());
    }

    /** Unit-price contracts: the contracted amount is not a ceiling, a known false positive of rule D. */
    static boolean isUnitPriceContract(Contract contract) {
        return matches(UNIT_PRICE_NOTE, contract.getNote()) || matches(UNIT_PRICE_NOTE, contract.getPaymentNote());
    }

    static List<EvidenceLink> evidence(Contract contract, List<String> fields) {
        List<EvidenceLink> links = new ArrayList<>();
        RawRecord raw = contract.getProvenance() == null ? null : contract.getProvenance().getRawRecord();
        if (raw != null) {
            SourceFile file = raw.getFile();
            String label = "Registar ugovora: " + file.getDataset().getTitle() + ", " + humanLocator(raw.getLocator());
            for (String field : fields) {
                links.add(new EvidenceLink(label, file.getUrl(), field));
            }
        }
        if (contract.getEojnContractUrl() != null) {
            String label = contract.isSynthetic() ? "Sintetički ugovor (nije stvarni zapis u EOJN-u)" : "Ugovor u EOJN-u";
            links.add(new EvidenceLink(label, contract.getEojnContractUrl(), null));
        }
        return links;
    }

    static String humanLocator(String locator) {
        Matcher matcher = LOCATOR.matcher(locator);
        return matcher.matches() ? "list " + matcher.group(1) + ", redak " + matcher.group(2) : locator;
    }

    /** "200.000,00 €", as in docs/UI_UX.md (hr-HR). */
    static String eur(BigDecimal amount) {
        DecimalFormat format = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.of("hr", "HR")));
        return format.format(amount) + " €";
    }

    /** 0.75 -> "75", 0.1001 -> "10,1", 215.803 -> "21.580,3". */
    static String percent(BigDecimal share) {
        DecimalFormat format = new DecimalFormat("#,##0.#", DecimalFormatSymbols.getInstance(Locale.of("hr", "HR")));
        format.setRoundingMode(RoundingMode.HALF_UP);
        return format.format(share.movePointRight(2));
    }

    /** {@code part / whole - 1} with enough precision to compare against thresholds such as 0.10 exactly. */
    static BigDecimal growth(BigDecimal part, BigDecimal whole) {
        return part.divide(whole, 6, RoundingMode.HALF_UP).subtract(BigDecimal.ONE);
    }

    private static boolean matches(Pattern pattern, String text) {
        return text != null && pattern.matcher(text).find();
    }
}
