package com.datathon.api.ingest.eojn;

import static com.datathon.api.ingest.eojn.EojnColumn.*;

import com.datathon.api.contract.SubjectType;
import com.datathon.api.ingest.IssueCode;
import com.datathon.api.ingest.SheetReader.SheetRow;
import com.datathon.api.ingest.normalize.Dates;
import com.datathon.api.ingest.normalize.Money;
import com.datathon.api.ingest.normalize.Oibs;
import com.datathon.api.ingest.normalize.VatCheck;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Normalises one data row of a class A register: amounts (HRK to EUR), dates (Excel serial or text), OIB checksum,
 * CPV. Rows without contract link, buyer, signing date or any amount are rejected (docs/ARCHITECTURE.md §6);
 * every other problem becomes a quality flag on the kept row.
 */
public final class EojnRowParser {

    public static final String EXTRACTOR_VERSION = "eojn-xlsx-v1";

    private static final Pattern CPV_CELL = Pattern.compile("^(\\d{8})(?:-\\d)?\\s*(?:-\\s*(.*))?$", Pattern.DOTALL);
    /** docs/ARCHITECTURE.md §6: amounts above this are flagged as implausible, not dropped. */
    private static final BigDecimal MAX_PLAUSIBLE_EUR = new BigDecimal("100000000");

    private final EojnHeader header;
    private final boolean isSyntheticSource;
    private final LocalDate today;

    public EojnRowParser(EojnHeader header, boolean isSyntheticSource, LocalDate today) {
        this.header = header;
        this.isSyntheticSource = isSyntheticSource;
        this.today = today;
    }

    public RowOutcome parse(SheetRow row) {
        if (row.isBlank()) {
            return new RowOutcome.Blank();
        }
        String contractUrl = text(row, CONTRACT_URL);
        if (contractUrl.isBlank()) {
            return new RowOutcome.Rejected(IssueCode.MISSING_CONTRACT_URL, "Redak nema poveznicu na ugovor (prirodni ključ).");
        }
        if (text(row, BUYER_NAME).isBlank() && text(row, BUYER_OIB).isBlank()) {
            return new RowOutcome.Rejected(IssueCode.MISSING_BUYER, "Redak nema naručitelja.");
        }
        String dateText = text(row, DATE_SIGNED);
        if (dateText.isBlank()) {
            return new RowOutcome.Rejected(IssueCode.MISSING_DATE_SIGNED, "Redak nema datum sklapanja.");
        }
        Optional<LocalDate> dateSigned = Dates.read(dateText);
        if (dateSigned.isEmpty()) {
            return new RowOutcome.Rejected(IssueCode.UNREADABLE_DATE_SIGNED, "Datum sklapanja nije čitljiv.");
        }
        return parseValidRow(row, contractUrl, dateSigned.get());
    }

    private RowOutcome parseValidRow(SheetRow row, String contractUrl, LocalDate dateSigned) {
        List<String> flags = new ArrayList<>();
        Amounts amounts = readAmounts(row, dateSigned, flags);
        if (amounts.initial().value() == null && amounts.total().value() == null) {
            return new RowOutcome.Rejected(IssueCode.MISSING_AMOUNT, "Redak nema čitljiv iznos ugovora.");
        }
        Oibs.Result buyerOib = readOib(row, BUYER_OIB, flags, IssueCode.BUYER_OIB_INVALID_CHECKSUM, IssueCode.BUYER_OIB_MALFORMED, null);
        Oibs.Result supplierOib = readOib(row, SUPPLIER_OIB, flags, IssueCode.SUPPLIER_OIB_INVALID_CHECKSUM,
                IssueCode.SUPPLIER_OIB_MALFORMED, IssueCode.SUPPLIER_OIB_MISSING);
        LocalDate completedOn = readOptionalDate(row, COMPLETED_ON, flags);
        Matcher cpv = CPV_CELL.matcher(text(row, CPV));
        boolean isCpvValid = cpv.matches();
        if (!isCpvValid) {
            flags.add(IssueCode.CPV_INVALID);
        }
        if (dateSigned.isAfter(today)) {
            flags.add(IssueCode.DATE_IN_FUTURE);
        }
        String subject = text(row, SUBJECT);
        if (subject.isBlank()) {
            flags.add(IssueCode.SUBJECT_MISSING);
        }
        return new RowOutcome.Parsed(new ParsedContract(
                contractUrl, blankToNull(text(row, TENDER_URL)), blankToNull(text(row, FRAMEWORK_AGREEMENT)),
                text(row, BUYER_NAME), buyerOib, blankToNull(text(row, SUPPLIER_NAME)), supplierOib,
                blankToNull(text(row, EVID_NUMBER)), blankToNull(text(row, CONTRACT_NUMBER)), subject,
                readSubjectType(text(row, SUBJECT_TYPE)),
                isCpvValid ? cpv.group(1) : null,
                isCpvValid ? blankToNull(cpv.group(2)) : blankToNull(text(row, CPV)),
                blankToNull(text(row, PROCEDURE_TYPE)), readYesNo(text(row, EU_FUNDED)), dateSigned,
                blankToNull(text(row, DURATION)), amounts.isHrk() ? "HRK" : "EUR",
                amounts.initial().value(), amounts.initialGross().value(), amounts.amendment().value(),
                amounts.total().value(), amounts.totalGross().value(), amounts.paid().value(), completedOn,
                blankToNull(text(row, PAYMENT_NOTE)), blankToNull(text(row, NOTE)), blankToNull(text(row, STATUS)),
                List.copyOf(flags)));
    }

    private record Amounts(Money.Result initial, Money.Result initialGross, Money.Result amendment,
                           Money.Result total, Money.Result totalGross, Money.Result paid) {

        boolean isHrk() {
            return initial.hrk() || total.hrk() || paid.hrk();
        }
    }

    private Amounts readAmounts(SheetRow row, LocalDate dateSigned, List<String> flags) {
        Amounts amounts = new Amounts(
                Money.read(text(row, INITIAL_VALUE), dateSigned), Money.read(text(row, INITIAL_VALUE_GROSS), dateSigned),
                Money.read(text(row, AMENDMENT_VALUE), dateSigned), Money.read(text(row, TOTAL_VALUE), dateSigned),
                Money.read(text(row, TOTAL_VALUE_GROSS), dateSigned), Money.read(text(row, PAID_AMOUNT), dateSigned));
        for (Money.Result amount : List.of(amounts.initial(), amounts.initialGross(), amounts.amendment(),
                amounts.total(), amounts.totalGross(), amounts.paid())) {
            addAmountFlag(amount, flags);
        }
        if (amounts.isHrk() && !dateSigned.isBefore(Money.EURO_ADOPTION)) {
            flags.add(IssueCode.HRK_AFTER_EURO);
        }
        if (isOutOfRange(amounts.initial().value()) || isOutOfRange(amounts.total().value())) {
            flags.add(IssueCode.AMOUNT_OUT_OF_RANGE);
        }
        VatCheck.addFlags(flags,
                new BigDecimal[] {amounts.initial().value(), amounts.initialGross().value()},
                new BigDecimal[] {amounts.total().value(), amounts.totalGross().value()});
        return amounts;
    }

    private static void addAmountFlag(Money.Result amount, List<String> flags) {
        String flag = switch (amount.status()) {
            case UNREADABLE -> IssueCode.UNREADABLE_AMOUNT;
            case PERIODIC -> IssueCode.PERIODIC_AMOUNT;
            default -> null;
        };
        if (flag != null && !flags.contains(flag)) {
            flags.add(flag);
        }
    }

    private static boolean isOutOfRange(BigDecimal value) {
        return value != null && (value.signum() <= 0 || value.compareTo(MAX_PLAUSIBLE_EUR) > 0);
    }

    private Oibs.Result readOib(SheetRow row, EojnColumn column, List<String> flags,
                                String invalidChecksumFlag, String malformedFlag, String missingFlag) {
        Oibs.Result result = Oibs.read(text(row, column), isSyntheticSource);
        String flag = switch (result.status()) {
            case INVALID_CHECKSUM -> invalidChecksumFlag;
            case MALFORMED -> malformedFlag;
            case MISSING -> missingFlag;
            default -> null;
        };
        if (flag != null) {
            flags.add(flag);
        }
        return result;
    }

    /** An unreadable optional date is kept as null with a flag, never guessed (docs/DATA_MODEL.md §4). */
    private LocalDate readOptionalDate(SheetRow row, EojnColumn column, List<String> flags) {
        String raw = text(row, column);
        if (raw.isBlank()) {
            return null;
        }
        Optional<LocalDate> date = Dates.read(raw);
        if (date.isEmpty()) {
            flags.add(IssueCode.UNREADABLE_COMPLETED_ON);
        }
        return date.orElse(null);
    }

    private static SubjectType readSubjectType(String raw) {
        return switch (raw.strip().toLowerCase(Locale.ROOT)) {
            case "robe" -> SubjectType.GOODS;
            case "usluge" -> SubjectType.SERVICES;
            case "radovi" -> SubjectType.WORKS;
            case "" -> null;
            default -> SubjectType.OTHER;
        };
    }

    private static Boolean readYesNo(String raw) {
        return switch (raw.strip().toUpperCase(Locale.ROOT)) {
            case "DA" -> Boolean.TRUE;
            case "NE" -> Boolean.FALSE;
            default -> null;
        };
    }

    private String text(SheetRow row, EojnColumn column) {
        return row.cell(header.column(column)).strip();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
