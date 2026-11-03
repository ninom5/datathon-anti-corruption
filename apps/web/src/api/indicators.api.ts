import {api} from "./base.ts";
import {toQueryParams} from "./params.ts";
import type {Indicator, IndicatorFilter, Page} from "@/types";

export const getIndicators = (params: IndicatorFilter) =>
    api.get<Page<Indicator>, Page<Indicator>>("/v1/indicators", {params: toQueryParams(params)});
