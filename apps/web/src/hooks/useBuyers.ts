import {useQuery} from "@tanstack/react-query";
import {getBuyers} from "@/api";

export const buyerKeys = {
    all: ["buyers"] as const,
    list: (synthetic: boolean) => [...buyerKeys.all, "list", synthetic] as const,
};

export const useBuyers = (synthetic: boolean) =>
    useQuery({
        queryKey: buyerKeys.list(synthetic),
        queryFn: () => getBuyers(synthetic),
        staleTime: 5 * 60 * 1000,
    });
