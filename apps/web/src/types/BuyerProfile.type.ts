import type {Indicator, RuleCode} from "./Indicator.type.ts";
import type {OrganizationRef} from "./Organization.type.ts";

export interface BuyerJpt {
    code: string;
    type: string;
    county: string | null;
}

export interface BuyerMunicipality {
    name: string;
    kind: "grad" | "opcina" | "grad_zagreb";
    county: string;
    population2021: number | null;
    peerGroup: string | null;
}

export interface BuyerCoverage {
    fromYear: number;
    toYear: number;
    years: number[];
    incompleteYears: number[];
    /** e.g. "2018.–2026., 2026. nepotpuna" */
    label: string;
}

export interface BuyerYearTotal {
    year: number;
    contracts: number;
    totalValue: number;
    incomplete: boolean;
}

export interface BuyerCpvTotal {
    cpv2: string;
    contracts: number;
    totalValue: number;
}

export interface BuyerSupplierTotal {
    supplier: OrganizationRef;
    contracts: number;
    totalValue: number;
}

export interface BuyerRuleCount {
    ruleCode: RuleCode;
    title: string;
    total: number;
    worthChecking: number;
    informative: number;
}

export interface BuyerSource {
    datasetTitle: string;
    landingUrl: string | null;
    formatClass: string | null;
    files: number;
    contracts: number;
}

/** GET /v1/buyers/{oib}. Numbers include crafts; topSuppliers leaves them out (craftContracts says how many). */
export interface BuyerProfile {
    buyer: OrganizationRef;
    jpt: BuyerJpt | null;
    municipality: BuyerMunicipality | null;
    coverage: BuyerCoverage;
    contracts: number;
    totalValue: number;
    byYear: BuyerYearTotal[];
    byCpv: BuyerCpvTotal[];
    topSuppliers: BuyerSupplierTotal[];
    craftContracts: number;
    indicatorCounts: BuyerRuleCount[];
    indicators: Indicator[];
    sources: BuyerSource[];
    synthetic: boolean;
}
