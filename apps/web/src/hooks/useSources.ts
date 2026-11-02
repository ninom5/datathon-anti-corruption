import {useQuery} from "@tanstack/react-query";
import {getSources, getSourcesSummary} from "@/api";

export const sourceKeys = {
    all: ["sources"] as const,
    list: (page: number, size: number) => [...sourceKeys.all, "list", page, size] as const,
    summary: () => [...sourceKeys.all, "summary"] as const,
};

export const useSources = (page: number, size: number) =>
    useQuery({
        queryKey: sourceKeys.list(page, size),
        queryFn: () => getSources(page, size),
    });

export const useSourcesSummary = () =>
    useQuery({
        queryKey: sourceKeys.summary(),
        queryFn: getSourcesSummary,
        staleTime: 5 * 60 * 1000,
    });
