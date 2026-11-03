package com.datathon.api.reference;

/** Reference sources verified with real requests on 2026-11-03 (docs/DATA_SOURCES.md §5, §6). */
public final class ReferenceSources {

    public static final String PORTAL_JPT = "jpt";
    public static final String JPT_DATASET = "popis-javnopravnih-tijela";
    public static final String JPT_LANDING = "https://data.gov.hr/dataset/popis-javnopravnih-tijela";

    public static final String PORTAL_DZS = "dzs";
    public static final String DZS_DATASET = "statistika-u-nizu-popis-stanovnistva-kucanstava-i-stanova-2021";
    public static final String DZS_LANDING = "https://data.gov.hr/dataset/" + DZS_DATASET;
    public static final String DZS_URL =
            "https://podaci.dzs.hr/media/td3jvrbu/popis_2021-stanovnistvo_po_gradovima_opcinama.xlsx";

    private ReferenceSources() {
    }
}
