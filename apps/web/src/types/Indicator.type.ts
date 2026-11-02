export type DataQuality = "niska" | "srednja" | "visoka";
export type EvidenceStrength = "slaba" | "srednja" | "jaka";
export type SeverityNote = "informativno" | "vrijedi_provjeriti";

export interface EvidenceLink {
    label: string;
    url: string;
    field: string | null;
}

export interface Magnitude {
    value: number | null;
    unit: string | null;
    label: string | null;
}

export interface Indicator {
    id: number;
    ruleCode: string;
    ruleVersion: string;
    title: string;
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
}

export interface IndicatorBadge {
    id: number;
    ruleCode: string;
    magnitudeValue: number | null;
    magnitudeLabel: string | null;
    severityNote: SeverityNote;
    possibleEntryError: boolean;
}
