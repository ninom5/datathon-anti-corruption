package com.datathon.api.ingest;

/** Codes for {@link IngestIssue}. Rejections drop the row; warnings keep it and lower data quality. */
public final class IssueCode {

    // --- file level (severity error) ---
    public static final String DOWNLOAD_FAILED = "download_failed";
    public static final String UNREADABLE_FILE = "unreadable_file";
    public static final String UNSUPPORTED_FORMAT = "unsupported_format";
    public static final String MISSING_REQUIRED_COLUMNS = "missing_required_columns";

    // --- row rejections (severity error), docs/ARCHITECTURE.md §6 ---
    public static final String MISSING_CONTRACT_URL = "missing_contract_url";
    public static final String MISSING_BUYER = "missing_buyer";
    public static final String MISSING_DATE_SIGNED = "missing_date_signed";
    public static final String UNREADABLE_DATE_SIGNED = "unreadable_date_signed";
    public static final String MISSING_AMOUNT = "missing_amount";

    // --- row warnings, stored in contract.quality_flags ---
    public static final String BUYER_OIB_INVALID_CHECKSUM = "buyer_oib_invalid_checksum";
    public static final String BUYER_OIB_MALFORMED = "buyer_oib_malformed";
    public static final String SUPPLIER_OIB_INVALID_CHECKSUM = "supplier_oib_invalid_checksum";
    public static final String SUPPLIER_OIB_MALFORMED = "supplier_oib_malformed";
    public static final String SUPPLIER_OIB_MISSING = "supplier_oib_missing";
    public static final String SUBJECT_MISSING = "subject_missing";
    public static final String CPV_INVALID = "cpv_invalid";
    public static final String UNREADABLE_AMOUNT = "unreadable_amount";
    public static final String PERIODIC_AMOUNT = "periodic_amount";
    public static final String AMOUNT_OUT_OF_RANGE = "amount_out_of_range";
    public static final String UNREADABLE_COMPLETED_ON = "unreadable_completed_on";
    public static final String DATE_IN_FUTURE = "date_in_future";
    public static final String HRK_AFTER_EURO = "hrk_after_euro";
    /** A2: the supplier cell names several OIBs (consortium); the first one, the leader, is used. */
    public static final String SUPPLIER_CONSORTIUM = "supplier_consortium";
    /** Amount with VAT below the amount without VAT: one of them is mistyped or in another currency in the source. */
    public static final String GROSS_BELOW_NET = "gross_below_net";
    /**
     * Amount with VAT more than 1,40 times the amount without VAT (VAT is at most 25 %; registration tax on cars adds a
     * little more). Real cases were a dropped decimal comma, e.g. Korčula 2022: 74.672,00 HRK without VAT and
     * 9.334.000,00 HRK with VAT.
     */
    public static final String VAT_RATIO_IMPLAUSIBLE = "vat_ratio_implausible";
    /**
     * The register lists the same contract twice under different EOJN links (same buyer, supplier, date, amount,
     * procurement number and contract number). Only the first row counts in group rules (F, C).
     */
    public static final String POSSIBLE_DUPLICATE = "possible_duplicate";
    /**
     * A2: the row has values to the right of the last header column, so its cells are shifted against the header
     * (Općina Cernik 2022: durations in the amount column, dates in the paid column). Its amounts are not trusted.
     */
    public static final String COLUMNS_SHIFTED = "columns_shifted";

    // --- dedup (severity warning, not attached to a contract) ---
    public static final String SUPERSEDED_BY_NEWER_FILE = "superseded_by_newer_file";
    /** A2 row not imported: the buyer has class A data for the same year (docs/DATA_MODEL.md §3). */
    public static final String OVERLAPS_CLASS_A = "overlaps_class_a";
    /** A2 file whose buyer could not be matched to an OIB in the JPT register. */
    public static final String BUYER_NOT_IN_JPT = "buyer_not_in_jpt";

    // --- reference data (JPT, DZS) ---
    public static final String REFERENCE_SOURCE_FAILED = "reference_source_failed";
    public static final String DZS_UNMATCHED = "dzs_unmatched";

    private IssueCode() {
    }
}
