package com.datathon.api.source;

/** Contract register layouts, see docs/DATA_SOURCES.md §1.1. Only A is parsed in the POC. */
public enum FormatClass {
    /** Standard EOJN export (33 columns, eojn.hr/contract-eo links). */
    A,
    /** Own table with supplier OIB. */
    B,
    /** Own table without OIB. */
    C,
    /** Unstructured (titles, merged cells, prose). */
    D
}
