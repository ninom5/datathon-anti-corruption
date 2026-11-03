import {api} from "./base.ts";
import type {BuyerOption, BuyerProfile} from "@/types";

export const getBuyers = (synthetic: boolean) =>
    api.get<BuyerOption[], BuyerOption[]>("/v1/buyers", {params: {synthetic}});

export const getBuyerProfile = (oib: string) =>
    api.get<BuyerProfile, BuyerProfile>(`/v1/buyers/${oib}`);
