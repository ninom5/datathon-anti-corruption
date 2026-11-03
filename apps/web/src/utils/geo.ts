import type {JlsGeometry, JlsShapes, Position} from "@/types";

export interface ProjectedShape {
    oib: string | null;
    name: string;
    path: string;
}

export interface ProjectedMap {
    width: number;
    height: number;
    shapes: ProjectedShape[];
}

const ringsOf = (geometry: JlsGeometry): Position[][] =>
    geometry.type === "Polygon" ? geometry.coordinates : geometry.coordinates.flat();

/**
 * Equirectangular projection scaled by cos(latitude) at the map centre. For a country the size of Croatia the
 * distortion is invisible, and it avoids a projection library (docs/UI_UX.md §6).
 */
export const projectShapes = (collection: JlsShapes, width: number): ProjectedMap => {
    // A loop instead of Math.min(...array): spreading ~100k coordinates can overflow the call stack.
    let minLon = Infinity, maxLon = -Infinity, minLat = Infinity, maxLat = -Infinity;
    for (const feature of collection.features) {
        for (const [lon, lat] of ringsOf(feature.geometry).flat()) {
            minLon = Math.min(minLon, lon);
            maxLon = Math.max(maxLon, lon);
            minLat = Math.min(minLat, lat);
            maxLat = Math.max(maxLat, lat);
        }
    }
    const xScale = Math.cos(((minLat + maxLat) / 2) * Math.PI / 180);
    const scale = width / ((maxLon - minLon) * xScale);
    const height = Math.round((maxLat - minLat) * scale);
    const toPoint = ([lon, lat]: Position) =>
        `${((lon - minLon) * xScale * scale).toFixed(1)},${((maxLat - lat) * scale).toFixed(1)}`;
    const shapes = collection.features.map((feature) => ({
        oib: feature.properties.oib,
        name: feature.properties.name,
        path: ringsOf(feature.geometry).map((ring) => `M${ring.map(toPoint).join("L")}Z`).join(""),
    }));
    return {width, height, shapes};
};
