import type {FeedbackKind, FeedbackRequest, FeedbackRole, FeedbackTargetType} from "@/types";
import {kindsNeedingContact, TEXT_MAX_LENGTH, TEXT_MIN_LENGTH} from "./feedbackOptions.ts";

export interface FeedbackDraft {
    kind: FeedbackKind;
    role: FeedbackRole | "";
    text: string;
    contactEmail: string;
}

export type FeedbackErrors = Partial<Record<"text" | "contactEmail", string>>;

export const emptyDraft: FeedbackDraft = {kind: "greska_u_podacima", role: "", text: "", contactEmail: ""};

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export const validateDraft = (draft: FeedbackDraft): FeedbackErrors => {
    const errors: FeedbackErrors = {};
    const textLength = draft.text.trim().length;
    if (textLength < TEXT_MIN_LENGTH || textLength > TEXT_MAX_LENGTH) {
        errors.text = `Opis mora imati od ${TEXT_MIN_LENGTH} do 4.000 znakova.`;
    }
    const email = draft.contactEmail.trim();
    if (email && !EMAIL_PATTERN.test(email)) {
        errors.contactEmail = "Upišite ispravnu e-mail adresu, npr. ime@primjer.hr.";
    }
    if (!email && kindsNeedingContact.includes(draft.kind)) {
        errors.contactEmail = "Za pravo na odgovor i uklanjanje podataka trebamo adresu za odgovor.";
    }
    return errors;
};

export const toRequest = (draft: FeedbackDraft, targetType: FeedbackTargetType, targetId: number): FeedbackRequest => ({
    targetType,
    targetId,
    kind: draft.kind,
    role: draft.role || undefined,
    text: draft.text.trim(),
    contactEmail: draft.contactEmail.trim() || undefined,
});
