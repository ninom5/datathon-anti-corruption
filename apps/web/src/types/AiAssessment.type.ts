export type AiLabel = "nema_oznake" | "vrijedi_provjeriti" | "visoka_pozornost";

export type AiConfidence = "niska" | "srednja" | "visoka";

export interface AiReason {
    claim: string;
    fields: string[];
    sources: string[];
}

export interface AiAssessment {
    id: number;
    label: AiLabel;
    confidence: AiConfidence;
    reasons: AiReason[];
    missingData: string[];
    legitExplanations: string[];
    model: string;
    promptVersion: string;
    createdAt: string;
    badge: string;
}
