import {api} from "./base.ts";
import type {JlsShapes, MunicipalityOverview, PeerComparison, PeerComparisonFilter} from "@/types";

export const getMunicipalities = () =>
    api.get<MunicipalityOverview[], MunicipalityOverview[]>("/v1/municipalities");

export const getPeerComparison = (oib: string, filter: PeerComparisonFilter) =>
    api.get<PeerComparison, PeerComparison>(`/v1/municipalities/${oib}/peers`, {params: filter});

/** Static file served by the web app itself, not by the API. */
export const getJlsShapes = () =>
    api.get<JlsShapes, JlsShapes>("/geo/jls.geojson", {baseURL: "/"});
