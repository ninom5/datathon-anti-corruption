/** JLS kind, as in the backend (DZS/JPT). */
export type MunicipalityKind = "grad" | "opcina" | "grad_zagreb";

/** One of the 556 JLS on the map. `contracts === 0` means no machine-readable register, not zero indicators. */
export interface MunicipalityOverview {
    oib: string;
    name: string;
    kind: MunicipalityKind;
    county: string;
    population2021: number | null;
    peerGroup: string | null;
    contracts: number;
    firstYear: number | null;
    lastYear: number | null;
    worthChecking: number;
    informative: number;
    /** Indicators "vrijedi provjeriti" per 100 contracts, null without contracts. */
    worthCheckingPer100: number | null;
}
