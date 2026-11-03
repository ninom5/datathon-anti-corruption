/** Longitude, latitude (WGS 84). */
export type Position = [number, number];

export type JlsGeometry =
    | {type: "Polygon"; coordinates: Position[][]}
    | {type: "MultiPolygon"; coordinates: Position[][][]};

export interface JlsShape {
    type: "Feature";
    /** oib is null for a shape that belongs to several JLS in the source (e.g. the island of Krk). */
    properties: {oib: string | null; name: string};
    geometry: JlsGeometry;
}

/** public/geo/jls.geojson: JLS boundaries with the OIB matched at build time (apps/web/scripts). */
export interface JlsShapes {
    type: "FeatureCollection";
    attribution: string;
    features: JlsShape[];
}
