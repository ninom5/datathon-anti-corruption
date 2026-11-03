"""Builds public/geo/jls.geojson: JLS boundaries with the OIB of each JLS, small enough for the browser.

Source: geoBoundaries HRV ADM2 (simplified), derived from OpenStreetMap. Licence and the reasons for this source
are in docs/DATA_SOURCES.md (karta). The OIB comes from our municipality table (JPT + DZS), read from the API.

Usage (API running, after an ingest):
    python3 -I scripts/build_jls_geojson.py <geoBoundaries-HRV-ADM2_simplified.geojson> http://localhost:8080

Prints the shapes that could not be assigned to one JLS (kept with oib null and drawn as "granice nisu dostupne")
and the JLS without a boundary (listed under the map); both lists go to docs/DATA_SOURCES.md.
"""
from __future__ import annotations

import json
import sys
import unicodedata
import urllib.request

OUTPUT = "public/geo/jls.geojson"
TOLERANCE_DEGREES = 0.002  # about 150-200 m, invisible at national scale
DECIMALS = 4
ATTRIBUTION = ("Granice JLS: geoBoundaries HRV ADM2 (CC BY-SA 2.0), izvedeno iz OpenStreetMapa "
               "(© OpenStreetMap suradnici, ODbL); pojednostavio JavniRadar.")

# OSM names that differ from the JPT/DZS name of the same JLS (typos in OSM, or an island of a JLS), checked by
# hand against the JLS list. Islands shared by several JLS (Krk, Cres with Osor, Kornati) are not assigned.
NAME_FIXES: dict[str, str] = {
    "Opicina Muter-Kornati": "Općina Murter - Kornati",
    "Opicina Pirovac": "Općina Pirovac",
    "Općina Hvratska Dubica": "Općina Hrvatska Dubica",
    "Općina Donji Kukuzari": "Općina Donji Kukuruzari",
    "Općina Magdenovac": "Općina Magadenovac",
    "Općina Veliki Pisanica": "Općina Velika Pisanica",
    "Grad Lobor": "Općina Lobor",
    "Otok Losinj": "Grad Mali Lošinj",
    "Otok Unije": "Grad Mali Lošinj",
    "Otok Susak": "Grad Mali Lošinj",
    "Otok Ilovik": "Grad Mali Lošinj",
    "Otok Vele Srakane": "Grad Mali Lošinj",
    "Otok Vele Orjule": "Grad Mali Lošinj",
}

# Two JLS called Privlaka (Zadarska and Vukovarsko-srijemska), told apart by longitude.
EASTERN_COUNTY = "VUKOVARSKO-SRIJEMSKA ŽUPANIJA"
EASTERN_FROM_LONGITUDE = 17.0


def normalize(name: str) -> str:
    """'GRAD BUJE - BUIE' and 'Grad Buje' -> 'buje': no prefix, no diacritics, Croatian part of bilingual names."""
    text = unicodedata.normalize("NFKD", name.lower())
    text = "".join(c for c in text if not unicodedata.combining(c)).replace("đ", "d")
    for prefix in ("grad ", "opcina "):
        if text.startswith(prefix):
            text = text[len(prefix):]
    text = text.split(" - ")[0]
    return " ".join(text.replace("-", " ").split())


def kind_of(name: str) -> str | None:
    lowered = name.lower()
    if lowered.startswith("grad zagreb"):
        return "grad_zagreb"
    if lowered.startswith("grad "):
        return "grad"
    if lowered.startswith(("općina ", "opcina ")):
        return "opcina"
    return None


def first_longitude(geometry: dict) -> float:
    ring = geometry["coordinates"][0] if geometry["type"] == "Polygon" else geometry["coordinates"][0][0]
    return sum(point[0] for point in ring) / len(ring)


def find_municipality(name: str, geometry: dict, by_name: dict) -> dict | None:
    candidates = by_name.get(normalize(name), [])
    if len(candidates) > 1:
        candidates = [m for m in candidates if m["kind"] == kind_of(name)]
    if len(candidates) > 1:
        is_eastern = first_longitude(geometry) > EASTERN_FROM_LONGITUDE
        candidates = [m for m in candidates if (m["county"] == EASTERN_COUNTY) == is_eastern]
    return candidates[0] if len(candidates) == 1 else None


def perpendicular_distance(point, start, end) -> float:
    (x, y), (x1, y1), (x2, y2) = point, start, end
    dx, dy = x2 - x1, y2 - y1
    if dx == 0 and dy == 0:
        return ((x - x1) ** 2 + (y - y1) ** 2) ** 0.5
    return abs(dy * x - dx * y + x2 * y1 - y2 * x1) / (dx * dx + dy * dy) ** 0.5


def simplify(points: list, tolerance: float) -> list:
    """Douglas-Peucker, iterative so long coastlines do not hit the recursion limit."""
    if len(points) < 3:
        return points
    keep = [False] * len(points)
    keep[0] = keep[-1] = True
    stack = [(0, len(points) - 1)]
    while stack:
        first, last = stack.pop()
        index, distance = None, tolerance
        for i in range(first + 1, last):
            d = perpendicular_distance(points[i], points[first], points[last])
            if d > distance:
                index, distance = i, d
        if index is not None:
            keep[index] = True
            stack.append((first, index))
            stack.append((index, last))
    return [p for p, k in zip(points, keep) if k]


def simplify_polygon(rings: list) -> list | None:
    result = []
    for position, ring in enumerate(rings):
        simplified = [[round(x, DECIMALS), round(y, DECIMALS)] for x, y in simplify(ring, TOLERANCE_DEGREES)]
        if len(simplified) >= 4:
            result.append(simplified)
        elif position == 0:
            return None  # outer ring vanished: an islet below the tolerance
    return result


def simplify_geometry(geometry: dict) -> dict:
    polygons = [geometry["coordinates"]] if geometry["type"] == "Polygon" else geometry["coordinates"]
    kept = [p for p in (simplify_polygon(polygon) for polygon in polygons) if p]
    if not kept:  # never drop a whole JLS: keep the largest polygon unsimplified
        kept = [max(polygons, key=lambda polygon: len(polygon[0]))]
    if len(kept) == 1:
        return {"type": "Polygon", "coordinates": kept[0]}
    return {"type": "MultiPolygon", "coordinates": kept}


def polygons_of(geometry: dict) -> list:
    return [geometry["coordinates"]] if geometry["type"] == "Polygon" else geometry["coordinates"]


def as_geometry(polygons: list) -> dict:
    if len(polygons) == 1:
        return {"type": "Polygon", "coordinates": polygons[0]}
    return {"type": "MultiPolygon", "coordinates": polygons}


def main(geo_path: str, api_base: str) -> None:
    with open(geo_path, encoding="utf-8") as file:
        boundaries = json.load(file)["features"]
    with urllib.request.urlopen(f"{api_base}/api/v1/municipalities") as response:
        municipalities = json.load(response)

    by_name: dict[str, list] = {}
    for municipality in municipalities:
        by_name.setdefault(normalize(municipality["name"]), []).append(municipality)

    # Several OSM shapes can belong to one JLS (Mali Lošinj and its islands), so polygons are grouped by OIB.
    polygons_by_oib: dict[str, list] = {}
    names_by_oib: dict[str, str] = {}
    unassigned = []
    for boundary in boundaries:
        geo_name = boundary["properties"]["shapeName"]
        geometry = simplify_geometry(boundary["geometry"])
        municipality = find_municipality(NAME_FIXES.get(geo_name, geo_name), boundary["geometry"], by_name)
        if municipality is None:
            unassigned.append({"type": "Feature", "properties": {"oib": None, "name": geo_name}, "geometry": geometry})
            continue
        polygons_by_oib.setdefault(municipality["oib"], []).extend(polygons_of(geometry))
        names_by_oib[municipality["oib"]] = municipality["name"]

    features = [{"type": "Feature", "properties": {"oib": oib, "name": names_by_oib[oib]},
                 "geometry": as_geometry(polygons)} for oib, polygons in polygons_by_oib.items()]
    missing = sorted(m["name"] for m in municipalities if m["oib"] not in polygons_by_oib)
    collection = {"type": "FeatureCollection", "attribution": ATTRIBUTION, "features": features + unassigned}
    with open(OUTPUT, "w", encoding="utf-8") as file:
        json.dump(collection, file, ensure_ascii=False, separators=(",", ":"))
    print(f"{len(features)} JLS and {len(unassigned)} unassigned shapes written to {OUTPUT}")
    print(f"Shapes without a JLS ({len(unassigned)}): {sorted(f['properties']['name'] for f in unassigned)}")
    print(f"JLS without a boundary ({len(missing)}): {missing}")


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2].rstrip("/"))
