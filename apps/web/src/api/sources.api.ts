import {api} from "./base.ts";
import type {Page, SourceDataset, SourcesSummary} from "@/types";

export const getSources = (page: number, size: number) =>
    api.get<Page<SourceDataset>, Page<SourceDataset>>("/v1/sources", {params: {page, size}});

export const getSourcesSummary = () =>
    api.get<SourcesSummary, SourcesSummary>("/v1/sources/summary");
