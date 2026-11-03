import type {AiAssessment} from "./AiAssessment.type.ts";
import type {Indicator, IndicatorBadge, MagnitudeBand, RuleCode} from "./Indicator.type.ts";
import type {OrganizationRef} from "./Organization.type.ts";
import type {Provenance} from "./Source.type.ts";

export type SubjectType = "robe" | "usluge" | "radovi" | "ostalo";

/** Amounts are EUR without VAT; null means "not published". */
export interface ContractListItem {
    id: number;
    buyer: OrganizationRef;
    supplier: OrganizationRef | null;
    subject: string;
    subjectType: SubjectType | null;
    dateSigned: string | null;
    year: number | null;
    yearIncomplete: boolean;
    initialValue: number | null;
    totalValue: number | null;
    paidAmount: number | null;
    indicators: IndicatorBadge[];
    synthetic: boolean;
}

export interface ContractValues {
    initialValue: number | null;
    initialValueGross: number | null;
    amendmentsValue: number | null;
    totalValue: number | null;
    totalValueGross: number | null;
    paidAmount: number | null;
    currency: string;
    convertedFromHrk: boolean;
}

export interface ContractDetail {
    id: number;
    buyer: OrganizationRef;
    supplier: OrganizationRef | null;
    subject: string;
    subjectType: SubjectType | null;
    cpv: {code: string; label: string | null} | null;
    procedureType: string | null;
    frameworkBased: boolean;
    contractNumber: string | null;
    dateSigned: string | null;
    year: number | null;
    yearIncomplete: boolean;
    durationText: string | null;
    completedOn: string | null;
    values: ContractValues;
    paymentNote: string | null;
    note: string | null;
    euFunded: boolean | null;
    qualityFlags: string[];
    eojnContractUrl: string | null;
    eojnTenderUrl: string | null;
    frameworkAgreementUrl: string | null;
    indicators: Indicator[];
    /** Null when there is no accepted AI assessment; then aiAssessmentUnavailableReason says why. */
    aiAssessment: AiAssessment | null;
    aiAssessmentUnavailableReason: string | null;
    provenance: Provenance;
    synthetic: boolean;
}

export type Severity = "vrijedi_provjeriti" | "informativno";

/** Query parameters of GET /v1/contracts and /v1/contracts/summary. */
export interface ContractFilter {
    buyerOib?: string;
    supplierOib?: string;
    year?: number;
    hasIndicator?: boolean;
    severity?: Severity;
    /** Contracts with an active indicator of one of these rules; combined with severity and band in one indicator. */
    ruleCode?: RuleCode[];
    band?: MagnitudeBand;
    synthetic?: boolean;
    sort?: "dateSigned" | "value";
    page?: number;
    size?: number;
}

export interface ContractSummary {
    contracts: number;
    totalValue: number;
    buyers: number;
    withIndicator: number;
    worthChecking: number;
    possibleEntryErrors: number;
    /** Contracts with crafts are counted in the numbers above; only lists with names leave them out. */
    craftContracts: number;
    /** Contracts with an indicator of each rule, under the filter without its own rule choice. */
    contractsByRule: Record<RuleCode, number>;
    incompleteYears: number[];
    syntheticContracts: number;
}
