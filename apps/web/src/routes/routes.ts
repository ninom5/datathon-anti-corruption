export const routes = {
    HOME: "/",
    CONTRACTS: "/istrazi",
    CONTRACT: "/ugovor/:id",
    BUYER: "/opcina/:oib",
    INDICATOR: "/indikator/:id",
    SOURCES: "/izvori",
} as const;

export const contractPath = (id: number) => `/ugovor/${id}`;

export const buyerPath = (oib: string) => `/opcina/${encodeURIComponent(oib)}`;
export const indicatorPath = (id: number) => `/indikator/${id}`;
