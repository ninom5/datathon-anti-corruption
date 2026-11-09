export type OrganizationType =
    | "jls" | "zupanija" | "javno_tijelo" | "doo" | "jdoo" | "dd" | "obrt" | "udruga"
    | "fizicka_osoba" | "strana" | "ostalo" | "nepoznato";

export interface OrganizationRef {
    id: number;
    oib: string | null;
    name: string;
    type: OrganizationType | null;
    nameHidden: boolean;
    synthetic: boolean;
}

export interface BuyerOption {
    id: number;
    oib: string | null;
    name: string;
    type: OrganizationType | null;
    contracts: number;
    synthetic: boolean;
}
