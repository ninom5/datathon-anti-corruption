import {api} from "./base.ts";
import {toQueryParams} from "./params.ts";
import type {Indicator, IndicatorDetail, IndicatorFilter, Page} from "@/types";

export const getIndicators = (params: IndicatorFilter) =>
    api.get<Page<Indicator>, Page<Indicator>>("/v1/indicators", {params: toQueryParams(params)});

export const getIndicator = (id: number) =>
    api.get<IndicatorDetail, IndicatorDetail>(`/v1/indicators/${id}`);
