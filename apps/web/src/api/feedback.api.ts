import {api} from "./base.ts";
import type {FeedbackRequest, FeedbackResponse} from "@/types";

export const postFeedback = (body: FeedbackRequest) =>
    api.post<FeedbackResponse, FeedbackResponse>("/v1/feedback", body);
