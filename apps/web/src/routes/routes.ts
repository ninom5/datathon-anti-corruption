export const routes = {
    HOME: "/",
    CONTRACTS: "/istrazi",
    CONTRACT: "/ugovor/:id",
    SOURCES: "/izvori",
} as const;

export const contractPath = (id: number) => `/ugovor/${id}`;
