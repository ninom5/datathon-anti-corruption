import type {IndicatorContract, OrganizationRef} from "@/types";

export interface TimelinePoint {
    contract: IndicatorContract;
    timestamp: number;
    amount: number;
    cumulative: number;
}

export const buildTimeline = (contracts: IndicatorContract[]): TimelinePoint[] | undefined => {
    const points: TimelinePoint[] = [];
    for (const contract of contracts) {
        const timestamp = contract.dateSigned ? Date.parse(contract.dateSigned) : NaN;
        if (!Number.isFinite(timestamp) || contract.initialValue === null || !Number.isFinite(contract.initialValue)) return undefined;
        points.push({contract, timestamp, amount: contract.initialValue, cumulative: 0});
    }
    points.sort((a, b) => a.timestamp - b.timestamp || a.contract.id - b.contract.id);
    let cents = 0;
    return points.map((point) => {
        cents += Math.round(point.amount * 100);
        return {...point, cumulative: cents / 100};
    });
};

export interface SupplierShare {
    id: number | null;
    supplier: OrganizationRef | null;
    contracts: number;
    value: number;
    share: number;
}

export const buildSupplierShares = (contracts: IndicatorContract[]): SupplierShare[] | undefined => {
    const groups = new Map<number | null, SupplierShare>();
    let totalCents = 0;
    for (const contract of contracts) {
        const amount = contract.totalValue ?? contract.initialValue;
        if (amount === null || !Number.isFinite(amount) || amount < 0) return undefined;
        const cents = Math.round(amount * 100);
        const id = contract.supplier?.id ?? null;
        const group = groups.get(id) ?? {id, supplier: contract.supplier, contracts: 0, value: 0, share: 0};
        group.value += cents;
        group.contracts += 1;
        totalCents += cents;
        groups.set(id, group);
    }
    if (totalCents <= 0) return undefined;
    return [...groups.values()].map((group) => ({...group, value: group.value / 100, share: group.value / totalCents}))
        .sort((a, b) => b.value - a.value || (a.id ?? 0) - (b.id ?? 0));
};

export const supplierChartLabel = (supplier: OrganizationRef | null) => {
    if (!supplier) return "Dobavljač nije objavljen";
    if (supplier.nameHidden || supplier.type === "obrt") return `Obrt (naziv skriven)${supplier.oib ? ` · OIB ${supplier.oib}` : ""}`;
    return supplier.name;
};

export const indicatorContext = (explanation: string) => {
    const starts = [...explanation.matchAll(/(?:CPV skupina \d+ obuhvaća|Prema registru javnopravnih tijela)/g)]
        .map((match) => match.index);
    return starts.length ? explanation.slice(Math.min(...starts)) : undefined;
};
