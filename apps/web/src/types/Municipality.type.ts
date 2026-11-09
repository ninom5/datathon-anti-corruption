export type MunicipalityKind = "grad" | "opcina" | "grad_zagreb";

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
    worthCheckingPer100: number | null;
}
