import type {MunicipalityOverview} from "@/types";

export type MapClass = "noData" | "c0" | "c1" | "c2" | "c3" | "c4";

export interface MapClassInfo {
    key: MapClass;
    label: string;
}

/**
 * Indicators "vrijedi provjeriti" per 100 contracts. Fixed breaks (not quantiles) so the colour of a JLS keeps its
 * meaning when data is added. "Bez podataka" is its own patterned class, never the lightest colour.
 */
export const MAP_CLASSES: MapClassInfo[] = [
    {key: "noData", label: "Bez strojno čitljivog registra"},
    {key: "c0", label: "0 (registar postoji)"},
    {key: "c1", label: "do 2"},
    {key: "c2", label: "2 – 5"},
    {key: "c3", label: "5 – 10"},
    {key: "c4", label: "više od 10"},
];

export const classOf = (municipality: MunicipalityOverview | undefined): MapClass => {
    const value = municipality?.worthCheckingPer100;
    if (value === null || value === undefined) {
        return "noData";
    }
    if (value === 0) return "c0";
    if (value <= 2) return "c1";
    if (value <= 5) return "c2";
    if (value <= 10) return "c3";
    return "c4";
};

/** Rates from a handful of contracts swing wildly, so the panel warns below this many. */
export const FEW_CONTRACTS = 20;
