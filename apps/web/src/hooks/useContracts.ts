import {keepPreviousData, useQuery} from "@tanstack/react-query";
import {getContract, getContracts, getContractSummary} from "@/api";
import type {ContractFilter} from "@/types";

export const contractKeys = {
    all: ["contracts"] as const,
    list: (filter: ContractFilter) => [...contractKeys.all, "list", filter] as const,
    summary: (filter: ContractFilter) => [...contractKeys.all, "summary", filter] as const,
    detail: (id: number) => [...contractKeys.all, "detail", id] as const,
};

export const useContracts = (filter: ContractFilter) =>
    useQuery({
        queryKey: contractKeys.list(filter),
        queryFn: () => getContracts(filter),
        placeholderData: keepPreviousData,
    });

const toSummaryFilter = (filter: ContractFilter): ContractFilter => ({
    buyerOib: filter.buyerOib,
    supplierOib: filter.supplierOib,
    year: filter.year,
    hasIndicator: filter.hasIndicator,
    severity: filter.severity,
    ruleCode: filter.ruleCode,
    band: filter.band,
    synthetic: filter.synthetic,
});

export const useContractSummary = (filter: ContractFilter) => {
    const summaryFilter = toSummaryFilter(filter);
    return useQuery({
        queryKey: contractKeys.summary(summaryFilter),
        queryFn: () => getContractSummary(summaryFilter),
        placeholderData: keepPreviousData,
    });
};

export const useContract = (id: number) =>
    useQuery({
        queryKey: contractKeys.detail(id),
        queryFn: () => getContract(id),
        enabled: Number.isInteger(id) && id > 0,
    });
