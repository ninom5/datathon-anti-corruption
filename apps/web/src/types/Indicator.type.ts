import type {OrganizationRef} from "./Organization.type.ts";

export type DataQuality = "niska" | "srednja" | "visoka";
export type EvidenceStrength = "slaba" | "srednja" | "jaka";
export type SeverityNote = "informativno" | "vrijedi_provjeriti";
export type RuleCode = "D_VALUE_INCREASE" | "D_OVERPAYMENT" | "F_SPLITTING" | "C_CONCENTRATION" | "B_YOUNG_COMPANY";
export type MagnitudeBand = "nizak" | "srednji" | "visok";
export type IndicatorSubjectType = "contract" | "group" | "buyer_cpv";

export interface EvidenceLink {
    label: string;
    url: string;
    field: string | null;
}

export interface Magnitude {
    value: number | null;
    unit: string | null;
    label: string | null;
    band: MagnitudeBand | null;
}

export interface Indicator {
    id: number;
    ruleCode: RuleCode;
    ruleVersion: string;
    title: string;
    subjectType: IndicatorSubjectType;
    buyer: OrganizationRef | null;
    contractIds: number[];
    magnitude: Magnitude;
    dataQuality: DataQuality;
    evidenceStrength: EvidenceStrength;
    severityNote: SeverityNote;
    possibleEntryError: boolean;
    sourceExplanation: string | null;
    explanation: string;
    notMeaning: string;
    missingData: string[];
    evidence: EvidenceLink[];
    computedAt: string;
    synthetic: boolean;
}

export interface IndicatorBadge {
    id: number;
    ruleCode: RuleCode;
    magnitudeValue: number | null;
    magnitudeLabel: string | null;
    magnitudeBand: MagnitudeBand | null;
    severityNote: SeverityNote;
    possibleEntryError: boolean;
}

export interface IndicatorFilter {
    ruleCode?: RuleCode[];
    buyerOib?: string;
    band?: MagnitudeBand;
    severity?: SeverityNote;
    synthetic?: boolean;
    page?: number;
    size?: number;
}

export interface IndicatorSubject {
    cpv?: string;
    cpvLevel?: number;
    from?: string;
    to?: string;
    threshold?: number;
    supplierOib?: string;
    cpv2?: string;
    years?: number[];
    topSupplierOib?: string;
}

export interface IndicatorContract {
    id: number;
    dateSigned: string | null;
    subject: string;
    cpv: string | null;
    supplier: OrganizationRef | null;
    initialValue: number | null;
    totalValue: number | null;
    procedureType: string | null;
}

export interface IndicatorDetail extends Indicator {
    contracts: IndicatorContract[];
    subject: IndicatorSubject | null;
}
