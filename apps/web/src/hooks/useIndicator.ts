import {useQuery} from "@tanstack/react-query";
import {getIndicator} from "@/api";

export const indicatorKeys = {
    all: ["indicators"] as const,
    detail: (id: number) => [...indicatorKeys.all, "detail", id] as const,
};

export const useIndicator = (id: number) => useQuery({
    queryKey: indicatorKeys.detail(id), queryFn: () => getIndicator(id), enabled: Number.isSafeInteger(id) && id > 0,
});
