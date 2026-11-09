import {type PointerEvent, useState} from "react";
import type {MunicipalityOverview} from "@/types";
import {formatNumber, plural, type ProjectedMap} from "@/utils";
import {classOf} from "./mapClasses.ts";
import styles from "./MapPage.module.css";

interface Hover {
    oib: string | null;
    name: string;
    x: number;
    y: number;
    isRightHalf: boolean;
}

interface TooltipProps {
    hover: Hover;
    municipality: MunicipalityOverview | undefined;
}

const Tooltip = ({hover, municipality}: TooltipProps) => {
    const describe = () => {
        if (!hover.oib) {
            return "Granice pojedinih JLS nisu dostupne u izvoru";
        }
        if (!municipality || municipality.contracts === 0) {
            return "Bez strojno čitljivog registra ugovora";
        }
        const contracts = `${formatNumber(municipality.contracts)} ${plural(municipality.contracts, "ugovor", "ugovora", "ugovora")}`;
        return `${contracts} · ${formatNumber(municipality.worthChecking)} vrijedi provjeriti`;
    };
    return (
        <div className={`${styles.tooltip} ${hover.isRightHalf ? styles.tooltipLeft : ""}`} style={{left: hover.x, top: hover.y}}
             aria-hidden="true">
            <strong>{municipality?.name ?? hover.name}</strong>
            <span>{describe()}</span>
        </div>
    );
};

interface JlsMapProps {
    map: ProjectedMap;
    municipalities: Map<string, MunicipalityOverview>;
    selectedOib: string | undefined;
    summary: string;
    onSelect: (oib: string) => void;
}

export const JlsMap = ({map, municipalities, selectedOib, summary, onSelect}: JlsMapProps) => {
    const [hover, setHover] = useState<Hover | undefined>();
    const selected = map.shapes.find((shape) => shape.oib !== null && shape.oib === selectedOib);

    const handlePointerMove = (event: PointerEvent<SVGGElement>) => {
        const target = event.target;
        const box = event.currentTarget.ownerSVGElement?.parentElement?.getBoundingClientRect();
        if (!(target instanceof SVGPathElement) || !box) {
            return;
        }
        setHover({
            oib: target.dataset.oib ?? null,
            name: target.dataset.name ?? "",
            x: event.clientX - box.left,
            y: event.clientY - box.top,
            isRightHalf: event.clientX - box.left > box.width / 2,
        });
    };

    const handleClick = (event: PointerEvent<SVGGElement>) => {
        const target = event.target;
        if (target instanceof SVGPathElement && target.dataset.oib) {
            onSelect(target.dataset.oib);
        }
    };

    return (
        <div className={styles.mapFrame}>
            <svg viewBox={`0 0 ${map.width} ${map.height}`} className={styles.map} role="img" aria-label={summary}>
                <defs>
                    <pattern id="no-data-hatch" width="6" height="6" patternUnits="userSpaceOnUse" patternTransform="rotate(45)">
                        <rect width="6" height="6" className={styles.noDataBase}/>
                        <line x1="0" y1="0" x2="0" y2="6" className={styles.noDataLine}/>
                    </pattern>
                </defs>
                <g onPointerMove={handlePointerMove} onPointerLeave={() => setHover(undefined)} onClick={handleClick}>
                    {map.shapes.map((shape) => (
                        <path
                            key={shape.oib ?? shape.name}
                            d={shape.path}
                            data-oib={shape.oib ?? undefined}
                            data-name={shape.name}
                            className={shape.oib ? `${styles.shape} ${styles[classOf(municipalities.get(shape.oib))]}` : styles.unassigned}
                        />
                    ))}
                </g>
                {selected && <path d={selected.path} className={styles.selectedOutline}/>}
            </svg>
            {hover && <Tooltip hover={hover} municipality={hover.oib ? municipalities.get(hover.oib) : undefined}/>}
        </div>
    );
};
