import {useMemo} from "react";
import {useSearchParams} from "wouter";
import {ErrorState, LoadingState, PageHeader, Select} from "@/components";
import {useJlsShapes, useMunicipalities} from "@/hooks";
import type {MunicipalityOverview} from "@/types";
import {formatNumber, projectShapes} from "@/utils";
import {JlsMap} from "./JlsMap.tsx";
import {JlsPanel} from "./JlsPanel.tsx";
import {JlsTable} from "./JlsTable.tsx";
import {MapLegend} from "./MapLegend.tsx";
import {classOf} from "./mapClasses.ts";
import styles from "./MapPage.module.css";

const MAP_WIDTH = 800;

const countByClass = (municipalities: MunicipalityOverview[]) => {
    const counts: Record<string, number> = {};
    for (const municipality of municipalities) {
        const key = classOf(municipality);
        counts[key] = (counts[key] ?? 0) + 1;
    }
    return counts;
};

export const MapPage = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const municipalitiesQuery = useMunicipalities();
    const shapesQuery = useJlsShapes();
    const shapes = shapesQuery.data;
    const projected = useMemo(() => (shapes ? projectShapes(shapes, MAP_WIDTH) : undefined), [shapes]);

    const selectedOib = searchParams.get("jls") ?? undefined;
    const handleSelect = (oib: string) => setSearchParams({jls: oib});

    if (municipalitiesQuery.error) {
        return <ErrorState message={municipalitiesQuery.error.message} onRetry={() => municipalitiesQuery.refetch()}/>;
    }
    if (shapesQuery.error) {
        return <ErrorState message={`Granice JLS se nisu učitale. ${shapesQuery.error.message}`} onRetry={() => shapesQuery.refetch()}/>;
    }
    if (municipalitiesQuery.isPending || !projected || !shapes) {
        return <LoadingState message="Učitavanje karte…"/>;
    }

    const municipalities = municipalitiesQuery.data;
    const byOib = new Map(municipalities.map((municipality) => [municipality.oib, municipality]));
    const withData = municipalities.filter((municipality) => municipality.contracts > 0);
    const worthChecking = withData.reduce((sum, municipality) => sum + municipality.worthChecking, 0);
    const onMap = new Set(projected.shapes.map((shape) => shape.oib));
    const withoutBoundary = municipalities.filter((municipality) => !onMap.has(municipality.oib));
    const summary = `Karta ${formatNumber(municipalities.length)} JLS: ${formatNumber(withData.length)} objavljuje strojno čitljiv `
        + `registar ugovora, ostale su šrafirane. Boja pokazuje broj indikatora „vrijedi provjeriti” na 100 ugovora.`;

    return (
        <>
            <PageHeader
                title="Karta: gdje ima podataka i indikatora"
                meta={<p className={styles.lead}>
                    <strong>{formatNumber(withData.length)} od {formatNumber(municipalities.length)}</strong> jedinica lokalne
                    samouprave objavljuje registar ugovora koji se može strojno pročitati. U njima je
                    {" "}<strong>{formatNumber(worthChecking)}</strong> indikatora koje vrijedi provjeriti. Šrafirane JLS
                    nemaju takav registar, što nije isto što i nula indikatora.
                </p>}
            />

            <label className={styles.quickPick}>
                Brzi odabir JLS s podacima
                <Select value={selectedOib && byOib.get(selectedOib)?.contracts ? selectedOib : ""}
                        onChange={(event) => event.target.value && handleSelect(event.target.value)}>
                    <option value="">Odaberite…</option>
                    {withData.map((municipality) => (
                        <option key={municipality.oib} value={municipality.oib}>{municipality.name}</option>
                    ))}
                </Select>
            </label>

            <div className={styles.layout}>
                <div>
                    <JlsMap map={projected} municipalities={byOib} selectedOib={selectedOib} summary={summary}
                            onSelect={handleSelect}/>
                    <MapLegend counts={countByClass(municipalities)}/>
                    <p className={styles.attribution}>{shapes.attribution}</p>
                    {withoutBoundary.length > 0 && (
                        <p className={styles.attribution}>
                            Bez granice u izvoru ({withoutBoundary.length}, nisu na karti, ali jesu u tablici):{" "}
                            {withoutBoundary.map((municipality) => municipality.name).join(", ")}.
                        </p>
                    )}
                </div>
                <JlsPanel municipality={selectedOib ? byOib.get(selectedOib) : undefined}
                          isWithoutBoundary={selectedOib !== undefined && !onMap.has(selectedOib)}/>
            </div>

            <details className={styles.tableDetails}>
                <summary>Prikaži kao tablicu</summary>
                <JlsTable municipalities={municipalities} onSelect={handleSelect}/>
            </details>
        </>
    );
};
