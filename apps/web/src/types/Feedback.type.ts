/** Codes as the backend sends them (docs/DATA_MODEL.md, table feedback). */
export type FeedbackTargetType = "indicator" | "contract";

export type FeedbackKind = "greska_u_podacima" | "kontekst" | "pravo_na_odgovor" | "uklanjanje_osobnih_podataka";

export type FeedbackRole = "gradanin" | "novinar" | "narucitelj" | "dobavljac" | "ostalo";

export type ModerationStatus = "novo" | "objavljeno" | "odbijeno";

export interface FeedbackRequest {
    targetType: FeedbackTargetType;
    targetId: number;
    kind: FeedbackKind;
    role?: FeedbackRole;
    text: string;
    contactEmail?: string;
}

export interface FeedbackResponse {
    id: number;
    targetType: FeedbackTargetType;
    targetId: number;
    kind: FeedbackKind;
    moderation: ModerationStatus;
    createdAt: string;
}
