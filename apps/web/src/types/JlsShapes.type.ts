export type Position = [number, number];

export type JlsGeometry =
    | {type: "Polygon"; coordinates: Position[][]}
    | {type: "MultiPolygon"; coordinates: Position[][][]};

export interface JlsShape {
    type: "Feature";
    properties: {oib: string | null; name: string};
    geometry: JlsGeometry;
}

export interface JlsShapes {
    type: "FeatureCollection";
    attribution: string;
    features: JlsShape[];
}
