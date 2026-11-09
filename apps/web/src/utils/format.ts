const LOCALE = "hr-HR";
export const NOT_PUBLISHED = "nije objavljeno";

const eurFormat = new Intl.NumberFormat(LOCALE, {style: "currency", currency: "EUR"});
const eurCompactFormat = new Intl.NumberFormat(LOCALE, {style: "currency", currency: "EUR", maximumFractionDigits: 0});
const numberFormat = new Intl.NumberFormat(LOCALE);
const percentFormat = new Intl.NumberFormat(LOCALE, {style: "percent", maximumFractionDigits: 1, signDisplay: "always"});
const dateFormat = new Intl.DateTimeFormat(LOCALE, {day: "2-digit", month: "2-digit", year: "numeric"});
const dateTimeFormat = new Intl.DateTimeFormat(LOCALE, {
    day: "2-digit", month: "2-digit", year: "numeric", hour: "2-digit", minute: "2-digit",
});

export const formatEur = (amount: number | null | undefined) =>
    amount === null || amount === undefined ? NOT_PUBLISHED : eurFormat.format(amount);

export const formatEurRounded = (amount: number | null | undefined) =>
    amount === null || amount === undefined ? NOT_PUBLISHED : eurCompactFormat.format(amount);

export const formatEurMillions = (amount: number) => {
    if (Math.abs(amount) < 1_000_000) {
        return eurCompactFormat.format(amount);
    }
    const nbsp = " ";
    return `${new Intl.NumberFormat(LOCALE, {maximumFractionDigits: 1}).format(amount / 1_000_000)}${nbsp}mil.${nbsp}€`;
};

export const formatNumber = (value: number) => numberFormat.format(value);

export const formatShare = (share: number) => percentFormat.format(share);

const ratioFormat = new Intl.NumberFormat(LOCALE, {maximumFractionDigits: 2});

export const formatMagnitude = (ruleCode: string, value: number) => {
    if (ruleCode === "B_YOUNG_COMPANY") {
        return `${numberFormat.format(value)} ${plural(value, "dan", "dana", "dana")}`;
    }
    if (ruleCode === "F_SPLITTING") {
        return `${ratioFormat.format(value)}×`;
    }
    if (ruleCode === "C_CONCENTRATION") {
        return `HHI ${numberFormat.format(Math.round(value))}`;
    }
    return formatShare(value);
};

export const formatDate = (isoDate: string | null | undefined) => {
    if (!isoDate) {
        return NOT_PUBLISHED;
    }
    const [year, month, day] = isoDate.slice(0, 10).split("-").map(Number);
    return dateFormat.format(new Date(year, month - 1, day));
};

export const formatDateTime = (isoInstant: string | null | undefined) =>
    isoInstant ? dateTimeFormat.format(new Date(isoInstant)) : NOT_PUBLISHED;

export const plural = (count: number, one: string, few: string, many: string) => {
    const lastTwo = count % 100;
    const last = count % 10;
    if (last === 1 && lastTwo !== 11) {
        return one;
    }
    if (last >= 2 && last <= 4 && (lastTwo < 12 || lastTwo > 14)) {
        return few;
    }
    return many;
};
