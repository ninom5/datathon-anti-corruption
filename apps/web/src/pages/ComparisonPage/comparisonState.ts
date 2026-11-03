import type {PeerComparisonFilter} from "@/types";

const OIB = /^\d{11}$/;
const CPV2 = /^\d{2}$/;

/** Comparison state kept in the URL (Croatian parameters, as in the explorer). */
export interface ComparisonState extends PeerComparisonFilter {
    oib?: string;
}

export const readComparisonState = (params: URLSearchParams): ComparisonState => {
    const oib = params.get("jls") ?? "";
    const year = Number(params.get("godina"));
    const cpv2 = params.get("cpv") ?? "";
    return {
        oib: OIB.test(oib) ? oib : undefined,
        year: Number.isInteger(year) && year > 1999 ? year : undefined,
        cpv2: CPV2.test(cpv2) ? cpv2 : undefined,
    };
};

export const writeComparisonState = (state: ComparisonState): Record<string, string> => {
    const params: Record<string, string> = {};
    if (state.oib) params.jls = state.oib;
    if (state.year) params.godina = String(state.year);
    if (state.cpv2) params.cpv = state.cpv2;
    return params;
};
