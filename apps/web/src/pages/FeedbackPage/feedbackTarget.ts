import type {FeedbackTargetType} from "@/types";

export interface FeedbackTargetRef {
    targetType: FeedbackTargetType;
    targetId: number;
}

const readId = (value: string | null) => (value && /^\d+$/.test(value) ? Number(value) : undefined);

/** `?indikator=12` or `?ugovor=34` (routes.feedbackPath); anything else has no target. */
export const readFeedbackTarget = (searchParams: URLSearchParams): FeedbackTargetRef | undefined => {
    const indicatorId = readId(searchParams.get("indikator"));
    if (indicatorId) {
        return {targetType: "indicator", targetId: indicatorId};
    }
    const contractId = readId(searchParams.get("ugovor"));
    return contractId ? {targetType: "contract", targetId: contractId} : undefined;
};
