import {api} from "./base.ts";
import {toQueryParams} from "./params.ts";
import type {ContractDetail, ContractFilter, ContractListItem, ContractSummary, Page} from "@/types";

export const getContracts = (params: ContractFilter) =>
    api.get<Page<ContractListItem>, Page<ContractListItem>>("/v1/contracts", {params: toQueryParams(params)});

export const getContractSummary = (params: ContractFilter) =>
    api.get<ContractSummary, ContractSummary>("/v1/contracts/summary", {params: toQueryParams(params)});

export const getContract = (id: number) =>
    api.get<ContractDetail, ContractDetail>(`/v1/contracts/${id}`);
