import {useState} from "react";
import type {PeerMember} from "@/types";
import {formatEur} from "@/utils";
import styles from "./ComparisonPage.module.css";

const WIDTH = 800;
// The top margin holds two text rows: the median label, and under it the name of the selected JLS.
const MARGIN = {left: 16, right: 24, top: 56, bottom: 36};
const RADIUS = 6;
const LANE = 16;

interface PlacedDot {
    member: PeerMember;
    x: number;
    lane: number;
}

const niceStep = (max: number) => {
    const raw = max / 4;
    const magnitude = 10 ** Math.floor(Math.log10(raw));
    const step = [1, 2, 2.5, 5, 10].map((factor) => factor * magnitude).find((candidate) => candidate >= raw);
    return step ?? raw;
};

/** Dots that would overlap move to the next lane up, so every JLS stays visible and hoverable. */
const placeDots = (members: PeerMember[], toX: (value: number) => number): PlacedDot[] => {
    const laneEnds: number[] = [];
    return [...members]
        .sort((a, b) => (a.valuePerCapita ?? 0) - (b.valuePerCapita ?? 0))
        .map((member) => {
            const x = toX(member.valuePerCapita ?? 0);
            let lane = laneEnds.findIndex((end) => x - end >= RADIUS * 2 + 2);
            if (lane === -1) {
                lane = laneEnds.length;
                laneEnds.push(x);
            } else {
                laneEnds[lane] = x;
            }
            return {member, x, lane};
        });
};

/** Near the edges the name is anchored inwards so it is not cut off. */
const labelAnchor = (x: number) => {
    if (x < WIDTH * 0.15) return "start";
    if (x > WIDTH * 0.85) return "end";
    return "middle";
};

interface DotPlotProps {
    members: PeerMember[];
    median: number | null;
    summary: string;
}

/** Strip plot of EUR per inhabitant in the peer group, with the median and the selected JLS (docs/UI_UX.md §3.5). */
export const DotPlot = ({members, median, summary}: DotPlotProps) => {
    const [hovered, setHovered] = useState<PlacedDot | undefined>();
    const plotted = members.filter((member) => member.valuePerCapita !== null);
    const max = Math.max(1, ...plotted.map((member) => member.valuePerCapita ?? 0));
    const step = niceStep(max);
    const axisMax = Math.ceil(max / step) * step;
    const toX = (value: number) => MARGIN.left + (value / axisMax) * (WIDTH - MARGIN.left - MARGIN.right);
    const dots = placeDots(plotted, toX);
    const lanes = Math.max(1, ...dots.map((dot) => dot.lane + 1));
    const height = MARGIN.top + lanes * LANE + MARGIN.bottom;
    const baseline = height - MARGIN.bottom;
    const ticks = Array.from({length: Math.round(axisMax / step) + 1}, (_, index) => index * step);
    const selected = dots.find((dot) => dot.member.selected);
    const yOf = (dot: PlacedDot) => baseline - LANE / 2 - dot.lane * LANE;

    return (
        <div className={styles.plotFrame}>
            <svg viewBox={`0 0 ${WIDTH} ${height}`} className={styles.plot} role="img" aria-label={summary}>
                {ticks.map((tick) => (
                    <g key={tick}>
                        <line x1={toX(tick)} x2={toX(tick)} y1={MARGIN.top - 8} y2={baseline} className={styles.grid}/>
                        <text x={toX(tick)} y={baseline + 18} className={styles.tick}>{formatEur(tick).replace(",00", "")}</text>
                    </g>
                ))}
                <line x1={MARGIN.left} x2={WIDTH - MARGIN.right} y1={baseline} y2={baseline} className={styles.axis}/>
                {median !== null && (
                    <g>
                        <line x1={toX(median)} x2={toX(median)} y1={20} y2={baseline} className={styles.median}/>
                        <text x={toX(median)} y={14} className={styles.medianLabel}>medijan {formatEur(median)}</text>
                    </g>
                )}
                {dots.map((dot) => (
                    <circle key={dot.member.oib} cx={dot.x} cy={yOf(dot)} r={RADIUS}
                            className={dot.member.selected ? styles.dotSelected : styles.dot}
                            onPointerEnter={() => setHovered(dot)} onPointerLeave={() => setHovered(undefined)}/>
                ))}
                {selected && (
                    <text x={selected.x} y={yOf(selected) - 12} className={styles.selectedLabel}
                          textAnchor={labelAnchor(selected.x)}>{selected.member.name}</text>
                )}
            </svg>
            {hovered && (
                <div className={styles.tooltip}
                     style={{left: `${(hovered.x / WIDTH) * 100}%`, top: `${(yOf(hovered) / height) * 100}%`}}
                     aria-hidden="true">
                    <strong>{hovered.member.name}</strong>
                    <span>{formatEur(hovered.member.valuePerCapita)} po stanovniku</span>
                </div>
            )}
        </div>
    );
};
