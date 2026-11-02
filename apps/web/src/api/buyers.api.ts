import {api} from "./base.ts";
import type {BuyerOption} from "@/types";

export const getBuyers = (synthetic: boolean) =>
    api.get<BuyerOption[], BuyerOption[]>("/v1/buyers", {params: {synthetic}});
