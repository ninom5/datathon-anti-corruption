package com.datathon.api.source;

/** Contract register layouts, see docs/DATA_SOURCES.md §1.1 and §1.1b. A and A2 are parsed. */
public enum FormatClass {
    /** Standard EOJN export (33 columns, eojn.hr/contract-eo links). */
    A,
    /** Old EOJN register report (18 columns, supplier name and OIB in one cell, no eojn.hr link). */
    A2,
    /** Own table with supplier OIB. */
    B,
    /** Own table without OIB. */
    C,
    /** Unstructured (titles, merged cells, prose). */
    D
}
