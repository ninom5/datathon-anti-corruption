/** AI label as returned by the API (docs/ANOMALY_DETECTION.md §4.3). */
export type AiLabel = "nema_oznake" | "vrijedi_provjeriti" | "visoka_pozornost";

export type AiConfidence = "niska" | "srednja" | "visoka";

export interface AiReason {
    claim: string;
    /** Context paths the claim is based on, e.g. "ugovor.predmet". */
    fields: string[];
    /** Source URLs from the context (register row, EOJN). */
    sources: string[];
}

/** An accepted AI assessment; always shown with its badge, model and prompt version, apart from the rules. */
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
