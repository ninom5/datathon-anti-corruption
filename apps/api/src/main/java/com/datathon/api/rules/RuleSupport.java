package com.datathon.api.rules;

import com.datathon.api.contract.Contract;
import com.datathon.api.contract.SubjectType;
import com.datathon.api.indicator.DataQuality;
import com.datathon.api.organization.Organization;
import com.datathon.api.organization.dto.OrganizationRef;
import com.datathon.api.source.FormatClass;
import com.datathon.api.source.Locators;
import com.datathon.api.source.RawRecord;
import com.datathon.api.source.SourceFile;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

final class RuleSupport {

    private static final Pattern UNIT_PRICE_NOTE = Pattern.compile("jedinič|jedinic", Pattern.CASE_INSENSITIVE);
    private static final Pattern FRAMEWORK_SUBJECT = Pattern.compile("okvirn|tromjesečn\\S* evidencij", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private RuleSupport() {
    }

    static DataQuality baseQuality(Contract contract, boolean hasDerivedInput) {
        return contract.getQualityFlags().isEmpty() && !hasDerivedInput ? DataQuality.HIGH : DataQuality.MEDIUM;
    }

    private static final Pattern CALL_OFF_RECORD = Pattern.compile("tromjesečn\\S* evidencij", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    static boolean isCallOffRecord(Contract contract) {
        return matches(CALL_OFF_RECORD, contract.getSubject());
    }

    static boolean isFrameworkContract(Contract contract) {
        return contract.isFrameworkBased() || (contract.getSubject() != null && FRAMEWORK_SUBJECT.matcher(contract.getSubject()).find());
    }

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
        return Locators.humanReadable(locator);
    }

    static String eur(BigDecimal amount) {
        DecimalFormat format = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.of("hr", "HR")));
        return format.format(amount) + " €";
    }

    static String percent(BigDecimal share) {
        DecimalFormat format = new DecimalFormat("#,##0.#", DecimalFormatSymbols.getInstance(Locale.of("hr", "HR")));
        format.setRoundingMode(RoundingMode.HALF_UP);
        return format.format(share.movePointRight(2));
    }

    static BigDecimal growth(BigDecimal part, BigDecimal whole) {
        return part.divide(whole, 6, RoundingMode.HALF_UP).subtract(BigDecimal.ONE);
    }

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy.");

    static DataQuality groupQuality(List<Contract> contracts, boolean hasDerivedInput) {
        boolean isAllClean = contracts.stream().allMatch(contract -> contract.getQualityFlags().isEmpty() && isClassA(contract));
        return isAllClean && !hasDerivedInput ? DataQuality.HIGH : DataQuality.MEDIUM;
    }

    private static boolean isClassA(Contract contract) {
        RawRecord raw = contract.getProvenance() == null ? null : contract.getProvenance().getRawRecord();
        return raw != null && raw.getFile().getFormatClass() == FormatClass.A;
    }

    static List<EvidenceLink> groupEvidence(List<Contract> contracts, String field) {
        List<EvidenceLink> links = new ArrayList<>();
        for (Contract contract : contracts) {
            RawRecord raw = contract.getProvenance() == null ? null : contract.getProvenance().getRawRecord();
            String what = date(contract.getDateSigned()) + ", " + eur(valueOf(contract));
            if (raw != null) {
                SourceFile file = raw.getFile();
                links.add(new EvidenceLink("Registar ugovora: " + file.getDataset().getTitle() + ", "
                        + humanLocator(raw.getLocator()) + " (" + what + ")", file.getUrl(), field));
            } else if (contract.getEojnContractUrl() != null) {
                links.add(new EvidenceLink("Ugovor u EOJN-u (" + what + ")", contract.getEojnContractUrl(), field));
            }
        }
        return links;
    }

    static BigDecimal valueOf(Contract contract) {
        return contract.getTotalValue() != null ? contract.getTotalValue() : contract.getInitialValue();
    }

    static String supplierName(Organization supplier) {
        String name = OrganizationRef.displayName(supplier);
        return supplier.getOib() == null ? name : name + " (OIB " + supplier.getOib() + ")";
    }

    static SubjectType thresholdType(Contract contract) {
        SubjectType type = contract.getSubjectType();
        if (type == SubjectType.WORKS || type == SubjectType.GOODS || type == SubjectType.SERVICES) {
            return type;
        }
        return contract.getCpv() != null && contract.getCpv().startsWith("45") ? SubjectType.WORKS : SubjectType.SERVICES;
    }

    static boolean isSubjectTypeDerived(Contract contract) {
        return contract.getSubjectType() == null || contract.getSubjectType() == SubjectType.OTHER;
    }

    static String cpvPrefix(Contract contract, int digits) {
        String cpv = contract.getCpv();
        return cpv == null || cpv.length() < digits ? null : cpv.substring(0, digits);
    }

    static String date(LocalDate date) {
        return date == null ? "datum nije objavljen" : DATE.format(date);
    }

    static String decimal(BigDecimal value) {
        DecimalFormat format = new DecimalFormat("#,##0.##", DecimalFormatSymbols.getInstance(Locale.of("hr", "HR")));
        format.setRoundingMode(RoundingMode.HALF_UP);
        return format.format(value);
    }

    private static boolean matches(Pattern pattern, String text) {
        return text != null && pattern.matcher(text).find();
    }
}
