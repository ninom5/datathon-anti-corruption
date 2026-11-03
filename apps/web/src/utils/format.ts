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

/** "1.234.567,89 €"; a missing amount is "nije objavljeno", never 0. */
export const formatEur = (amount: number | null | undefined) =>
    amount === null || amount === undefined ? NOT_PUBLISHED : eurFormat.format(amount);

/** Whole euros, for tables and summaries ("1.234.568 €"). */
export const formatEurRounded = (amount: number | null | undefined) =>
    amount === null || amount === undefined ? NOT_PUBLISHED : eurCompactFormat.format(amount);

/** "99,4 mil. €" for large totals in summary lines. */
export const formatEurMillions = (amount: number) => {
    if (Math.abs(amount) < 1_000_000) {
        return eurCompactFormat.format(amount);
    }
    const nbsp = " "; // keeps "99,4 mil. €" on one line
    return `${new Intl.NumberFormat(LOCALE, {maximumFractionDigits: 1}).format(amount / 1_000_000)}${nbsp}mil.${nbsp}€`;
};

export const formatNumber = (value: number) => numberFormat.format(value);

/** 0.75 -> "+75 %". */
export const formatShare = (share: number) => percentFormat.format(share);

const ratioFormat = new Intl.NumberFormat(LOCALE, {maximumFractionDigits: 2});

/**
 * Short magnitude for badges, in the unit of each rule: D1/D2 a share ("+75 %"), F sum / threshold ("2,49×"),
 * C the HHI ("HHI 8.580").
 */
export const formatMagnitude = (ruleCode: string, value: number) => {
    if (ruleCode === "F_SPLITTING") {
        return `${ratioFormat.format(value)}×`;
    }
    if (ruleCode === "C_CONCENTRATION") {
        return `HHI ${numberFormat.format(Math.round(value))}`;
    }
    return formatShare(value);
};

/** ISO date "2025-03-10" -> "10. 03. 2025.". Parsed as a local date so the day does not shift by time zone. */
export const formatDate = (isoDate: string | null | undefined) => {
    if (!isoDate) {
        return NOT_PUBLISHED;
    }
    const [year, month, day] = isoDate.slice(0, 10).split("-").map(Number);
    return dateFormat.format(new Date(year, month - 1, day));
};

export const formatDateTime = (isoInstant: string | null | undefined) =>
    isoInstant ? dateTimeFormat.format(new Date(isoInstant)) : NOT_PUBLISHED;

/** Croatian plural: 1 ugovor, 2 ugovora, 5 ugovora, 21 ugovor. */
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
