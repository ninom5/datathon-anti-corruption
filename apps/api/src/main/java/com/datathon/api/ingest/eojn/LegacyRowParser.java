package com.datathon.api.ingest.eojn;

import static com.datathon.api.ingest.eojn.LegacyColumn.*;

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
 * Normalises one data row of an old EOJN register report (A2). Same rules as {@link EojnRowParser}, with three
 * differences forced by the format: the buyer comes from the file (title line or publisher), the supplier name and
 * OIB share one cell, and there is no amendment column, so only the initial value is filled (total stays null and
 * rule D1 skips the row; D2 compares the paid amount with the initial gross value).
 */
public final class LegacyRowParser {

    public static final String EXTRACTOR_VERSION = "eojn-legacy-xlsx-v1";

    private static final Pattern CPV_CELL = Pattern.compile("^(\\d{8})(?:-\\d)?\\s*(?:-\\s*(.*))?$", Pattern.DOTALL);
    private static final Pattern KUNA_HEADER = Pattern.compile("\\(kn\\)|\\bu kn\\b|\\bhrk\\b");
    private static final BigDecimal MAX_PLAUSIBLE_EUR = new BigDecimal("100000000");

    private final LegacyHeader header;
    private final SheetRow headerRow;
    private final String buyerName;
    private final Oibs.Result buyerOib;
    private final LocalDate today;

    public LegacyRowParser(LegacyHeader header, SheetRow headerRow, String buyerName, Oibs.Result buyerOib, LocalDate today) {
        this.header = header;
        this.headerRow = headerRow;
        this.buyerName = buyerName;
        this.buyerOib = buyerOib;
        this.today = today;
    }

    public RowOutcome parse(SheetRow row) {
        if (row.isBlank()) {
            return new RowOutcome.Blank();
        }
        String dateText = text(row, DATE_SIGNED);
        if (dateText.isBlank()) {
            // Sub-total and footer lines of the EOJN report have no date and no supplier.
            return text(row, SUPPLIER).isBlank() && text(row, SUBJECT).isBlank()
                    ? new RowOutcome.Blank()
                    : new RowOutcome.Rejected(IssueCode.MISSING_DATE_SIGNED, "Redak nema datum sklapanja.");
        }
        Optional<LocalDate> dateSigned = Dates.read(dateText);
        if (dateSigned.isEmpty()) {
            return new RowOutcome.Rejected(IssueCode.UNREADABLE_DATE_SIGNED, "Datum sklapanja nije čitljiv.");
        }
        return parseValidRow(row, dateSigned.get());
    }

    private RowOutcome parseValidRow(SheetRow row, LocalDate dateSigned) {
        List<String> flags = new ArrayList<>();
        if (hasCellsBeyondHeader(row)) {
            flags.add(IssueCode.COLUMNS_SHIFTED);
        }
        Money.Result initial = readAmount(row, INITIAL_VALUE, dateSigned, flags);
        Money.Result initialGross = readAmount(row, INITIAL_VALUE_GROSS, dateSigned, flags);
        Money.Result paid = readAmount(row, PAID_AMOUNT, dateSigned, flags);
        if (initial.value() == null && initialGross.value() == null) {
            return new RowOutcome.Rejected(IssueCode.MISSING_AMOUNT, "Redak nema čitljiv iznos ugovora.");
        }
        boolean isHrk = initial.hrk() || initialGross.hrk() || paid.hrk();
        if (isHrk && !dateSigned.isBefore(Money.EURO_ADOPTION)) {
            flags.add(IssueCode.HRK_AFTER_EURO);
        }
        if (isOutOfRange(initial.value())) {
            flags.add(IssueCode.AMOUNT_OUT_OF_RANGE);
        }
        VatCheck.check(initial.value(), initialGross.value()).ifPresent(flags::add);
        SupplierCell supplier = SupplierCell.split(text(row, SUPPLIER));
        Oibs.Result supplierOib = readSupplierOib(supplier, flags);
        if (supplier.isConsortium()) {
            flags.add(IssueCode.SUPPLIER_CONSORTIUM);
        }
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
                null, null, null, buyerName, buyerOib, supplier.name(), supplierOib,
                blankToNull(text(row, EVID_NUMBER)), blankToNull(text(row, CONTRACT_NUMBER)), subject, null,
                isCpvValid ? cpv.group(1) : null,
                isCpvValid ? blankToNull(cpv.group(2)) : blankToNull(text(row, CPV)),
                blankToNull(text(row, PROCEDURE_TYPE)), readYesNo(text(row, EU_FUNDED)), dateSigned,
                blankToNull(text(row, DURATION)), isHrk ? "HRK" : "EUR",
                initial.value(), initialGross.value(), null, null, null, paid.value(),
                readOptionalDate(row, flags), blankToNull(text(row, PAYMENT_NOTE)), blankToNull(text(row, NOTE)), null,
                List.copyOf(flags)));
    }

    /**
     * Yearly sheets put the currency in the header ("Iznos bez PDV-a (kn)") and leave plain numbers in the cells;
     * the marker is carried over so that Money converts correctly also for contracts signed after 2022.
     */
    private Money.Result readAmount(SheetRow row, LegacyColumn column, LocalDate dateSigned, List<String> flags) {
        Money.Result amount = Money.read(text(row, column), dateSigned, headerCurrency(column));
        String flag = switch (amount.status()) {
            case UNREADABLE -> IssueCode.UNREADABLE_AMOUNT;
            case PERIODIC -> IssueCode.PERIODIC_AMOUNT;
            default -> null;
        };
        if (flag != null && !flags.contains(flag)) {
            flags.add(flag);
        }
        return amount;
    }

    private Money.HeaderCurrency headerCurrency(LegacyColumn column) {
        String headerText = header.column(column) < 0 ? "" : header.headerText(headerRow, column);
        if (KUNA_HEADER.matcher(headerText).find()) {
            return Money.HeaderCurrency.HRK;
        }
        return headerText.contains("eur") ? Money.HeaderCurrency.EUR : Money.HeaderCurrency.NONE;
    }

    private static Oibs.Result readSupplierOib(SupplierCell supplier, List<String> flags) {
        Oibs.Result result = Oibs.read(supplier.oibText(), false);
        String flag = switch (result.status()) {
            case INVALID_CHECKSUM -> IssueCode.SUPPLIER_OIB_INVALID_CHECKSUM;
            case MALFORMED -> IssueCode.SUPPLIER_OIB_MALFORMED;
            case MISSING -> IssueCode.SUPPLIER_OIB_MISSING;
            default -> null;
        };
        if (flag != null) {
            flags.add(flag);
        }
        return result;
    }

    private LocalDate readOptionalDate(SheetRow row, List<String> flags) {
        String raw = text(row, COMPLETED_ON);
        if (raw.isBlank()) {
            return null;
        }
        Optional<LocalDate> date = Dates.read(raw);
        if (date.isEmpty()) {
            flags.add(IssueCode.UNREADABLE_COMPLETED_ON);
        }
        return date.orElse(null);
    }

    private boolean hasCellsBeyondHeader(SheetRow row) {
        int lastHeaderColumn = -1;
        for (int index = 0; index < headerRow.cells().size(); index++) {
            if (!headerRow.cell(index).isBlank()) {
                lastHeaderColumn = index;
            }
        }
        for (int index = lastHeaderColumn + 1; index < row.cells().size(); index++) {
            if (!row.cell(index).isBlank()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isOutOfRange(BigDecimal value) {
        return value != null && (value.signum() <= 0 || value.compareTo(MAX_PLAUSIBLE_EUR) > 0);
    }

    private static Boolean readYesNo(String raw) {
        return switch (raw.strip().toUpperCase(Locale.ROOT)) {
            case "DA" -> Boolean.TRUE;
            case "NE" -> Boolean.FALSE;
            default -> null;
        };
    }

    private String text(SheetRow row, LegacyColumn column) {
        return row.cell(header.column(column)).strip();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
