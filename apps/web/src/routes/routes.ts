export const routes = {
    HOME: "/",
    CONTRACTS: "/istrazi",
    CONTRACT: "/ugovor/:id",
    BUYER: "/opcina/:oib",
    INDICATOR: "/indikator/:id",
    SOURCES: "/izvori",
    MAP: "/karta",
    COMPARISON: "/usporedba",
    FEEDBACK: "/ispravak",
    METHODOLOGY: "/metodologija",
} as const;

export const contractPath = (id: number) => `/ugovor/${id}`;

export const buyerPath = (oib: string) => `/opcina/${encodeURIComponent(oib)}`;
export const indicatorPath = (id: number) => `/indikator/${id}`;

/** Explorer filtered to one buyer (URL parameters as in ContractsPage/explorerState.ts). */
export const contractsOfBuyerPath = (oib: string) => `/istrazi?${new URLSearchParams({narucitelj: oib})}`;

export const comparisonPath = (oib: string) => `/usporedba?${new URLSearchParams({jls: oib})}`;

export const mapPath = (oib: string) => `/karta?${new URLSearchParams({jls: oib})}`;

/** Correction form for one indicator or contract (F12); the target is in the URL so the link can be shared. */
export const feedbackPath = (target: "indikator" | "ugovor", id: number) =>
    `/ispravak?${new URLSearchParams({[target]: String(id)})}`;
