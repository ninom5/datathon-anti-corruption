/** Database codes of organization types (docs/DATA_MODEL.md §2). Buyers are "jls", "zupanija" or "javno_tijelo". */
export type OrganizationType =
    | "jls" | "zupanija" | "javno_tijelo" | "doo" | "jdoo" | "dd" | "obrt" | "udruga"
    | "fizicka_osoba" | "strana" | "ostalo" | "nepoznato";

/** A craft's name is hidden by the API (GDPR): name is "Obrt (naziv skriven)" and nameHidden is true. */
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
