package com.datathon.api.ingest;

public final class IssueCode {

    public static final String DOWNLOAD_FAILED = "download_failed";
    public static final String UNREADABLE_FILE = "unreadable_file";
    public static final String UNSUPPORTED_FORMAT = "unsupported_format";
    public static final String MISSING_REQUIRED_COLUMNS = "missing_required_columns";

    public static final String MISSING_CONTRACT_URL = "missing_contract_url";
    public static final String MISSING_BUYER = "missing_buyer";
    public static final String MISSING_DATE_SIGNED = "missing_date_signed";
    public static final String UNREADABLE_DATE_SIGNED = "unreadable_date_signed";
    public static final String MISSING_AMOUNT = "missing_amount";

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
    public static final String SUPPLIER_CONSORTIUM = "supplier_consortium";
    public static final String GROSS_BELOW_NET = "gross_below_net";
    public static final String VAT_RATIO_IMPLAUSIBLE = "vat_ratio_implausible";
    public static final String POSSIBLE_DUPLICATE = "possible_duplicate";
    public static final String COLUMNS_SHIFTED = "columns_shifted";

    public static final String SUPERSEDED_BY_NEWER_FILE = "superseded_by_newer_file";
    public static final String OVERLAPS_CLASS_A = "overlaps_class_a";
    public static final String BUYER_NOT_IN_JPT = "buyer_not_in_jpt";

    public static final String REFERENCE_SOURCE_FAILED = "reference_source_failed";
    public static final String DZS_UNMATCHED = "dzs_unmatched";

    private IssueCode() {
    }
}
