import type {MunicipalityKind} from "./Municipality.type.ts";

export interface PeerYearOption {
    year: number;
    withData: number;
    incomplete: boolean;
}

export interface PeerCategoryOption {
    cpv2: string;
    name: string;
    municipalities: number;
    contracts: number;
    totalValue: number;
}

export interface PeerMember {
    oib: string;
    name: string;
    population2021: number | null;
    contractsInYear: number;
    contracts: number;
    totalValue: number;
    valuePerCapita: number | null;
    selected: boolean;
}

export interface PeerComparison {
    municipality: {
        oib: string;
        name: string;
        kind: MunicipalityKind;
        county: string;
        population2021: number | null;
    };
    peerGroup: string;
    peerGroupLabel: string;
    year: number;
    yearIncomplete: boolean;
    years: PeerYearOption[];
    cpv2: string | null;
    cpvName: string | null;
    categories: PeerCategoryOption[];
    groupSize: number;
    withData: number;
    minimumForComparison: number;
    sufficient: boolean;
    medianPerCapita: number | null;
    selectedRank: number | null;
    members: PeerMember[];
}

export interface PeerComparisonFilter {
    year?: number;
    cpv2?: string;
}
