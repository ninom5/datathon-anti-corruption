import type {ContractFilter} from "@/types";

export type IndicatorChoice = "all" | "any" | "worth" | "error" | "none";
export type SortChoice = "dateSigned" | "value";

/** Explorer state kept in the URL, so a filtered view can be shared or reopened. */
export interface ExplorerState {
    buyerOib?: string;
    supplierOib?: string;
    year?: number;
    indicator: IndicatorChoice;
    synthetic: boolean;
    sort: SortChoice;
    page: number;
}

export const PAGE_SIZE = 25;
const OIB = /^\d{11}$/;
const INDICATOR_CHOICES: IndicatorChoice[] = ["all", "any", "worth", "error", "none"];

export const isOib = (value: string) => OIB.test(value);

export const readExplorerState = (params: URLSearchParams): ExplorerState => {
    const buyer = params.get("narucitelj") ?? "";
    const supplier = params.get("dobavljac") ?? "";
    const year = Number(params.get("godina"));
    const indicator = params.get("oznake") as IndicatorChoice | null;
    const page = Number(params.get("stranica"));
    return {
        buyerOib: isOib(buyer) ? buyer : undefined,
        supplierOib: isOib(supplier) ? supplier : undefined,
        year: Number.isInteger(year) && year > 1999 ? year : undefined,
        indicator: indicator && INDICATOR_CHOICES.includes(indicator) ? indicator : "all",
        synthetic: params.get("sinteticki") === "da",
        sort: params.get("poredak") === "iznos" ? "value" : "dateSigned",
        page: Number.isInteger(page) && page > 1 ? page - 1 : 0,
    };
};

/** URL parameters are Croatian because users see and share them. Defaults are left out to keep links short. */
export const writeExplorerState = (state: ExplorerState): Record<string, string> => {
    const params: Record<string, string> = {};
    if (state.buyerOib) params.narucitelj = state.buyerOib;
    if (state.supplierOib) params.dobavljac = state.supplierOib;
    if (state.year) params.godina = String(state.year);
    if (state.indicator !== "all") params.oznake = state.indicator;
    if (state.synthetic) params.sinteticki = "da";
    if (state.sort === "value") params.poredak = "iznos";
    if (state.page > 0) params.stranica = String(state.page + 1);
    return params;
};

const indicatorFilter = (choice: IndicatorChoice): Pick<ContractFilter, "hasIndicator" | "severity"> => {
    switch (choice) {
        case "any":
            return {hasIndicator: true};
        case "worth":
            return {severity: "vrijedi_provjeriti"};
        case "error":
            return {severity: "informativno"};
        case "none":
            return {hasIndicator: false};
        default:
            return {};
    }
};

export const toContractFilter = (state: ExplorerState): ContractFilter => ({
    buyerOib: state.buyerOib,
    supplierOib: state.supplierOib,
    year: state.year,
    ...indicatorFilter(state.indicator),
    synthetic: state.synthetic || undefined,
    sort: state.sort,
    page: state.page,
    size: PAGE_SIZE,
});
