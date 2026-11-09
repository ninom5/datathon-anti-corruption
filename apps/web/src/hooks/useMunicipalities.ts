import {keepPreviousData, useQuery} from "@tanstack/react-query";
import {getJlsShapes, getMunicipalities, getPeerComparison} from "@/api";
import type {PeerComparisonFilter} from "@/types";

export const municipalityKeys = {
    all: ["municipalities"] as const,
    list: () => [...municipalityKeys.all, "list"] as const,
    shapes: () => [...municipalityKeys.all, "shapes"] as const,
    peers: (oib: string, filter: PeerComparisonFilter) => [...municipalityKeys.all, "peers", oib, filter] as const,
};

export const useMunicipalities = () =>
    useQuery({
        queryKey: municipalityKeys.list(),
        queryFn: getMunicipalities,
        staleTime: 5 * 60 * 1000,
    });

export const useJlsShapes = () =>
    useQuery({
        queryKey: municipalityKeys.shapes(),
        queryFn: getJlsShapes,
        staleTime: Infinity,
    });

export const usePeerComparison = (oib: string | undefined, filter: PeerComparisonFilter) =>
    useQuery({
        queryKey: municipalityKeys.peers(oib ?? "", filter),
        queryFn: () => getPeerComparison(oib ?? "", filter),
        enabled: oib !== undefined,
        placeholderData: keepPreviousData,
    });
