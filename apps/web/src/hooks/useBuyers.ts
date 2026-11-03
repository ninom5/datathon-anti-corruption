import {useQuery} from "@tanstack/react-query";
import {getBuyerProfile, getBuyers} from "@/api";

export const buyerKeys = {
    all: ["buyers"] as const,
    profile: (oib: string) => [...buyerKeys.all, "profile", oib] as const,
    list: (synthetic: boolean) => [...buyerKeys.all, "list", synthetic] as const,
};

export const useBuyers = (synthetic: boolean) =>
    useQuery({
        queryKey: buyerKeys.list(synthetic),
        queryFn: () => getBuyers(synthetic),
        staleTime: 5 * 60 * 1000,
    });

export const useBuyerProfile = (oib: string) =>
    useQuery({queryKey: buyerKeys.profile(oib), queryFn: () => getBuyerProfile(oib), enabled: /^\d{11}$/.test(oib)});
