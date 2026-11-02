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

    // --- dedup (severity warning, not attached to a contract) ---
    public static final String SUPERSEDED_BY_NEWER_FILE = "superseded_by_newer_file";

    private IssueCode() {
    }
}
