/**
 * Spring binds a list from one comma-separated value ("D_VALUE_INCREASE,F_SPLITTING"); axios would send
 * "ruleCode[]=..." instead, so array parameters are joined here.
 */
export const toQueryParams = <T extends object>(params: T): Record<string, unknown> =>
    Object.fromEntries(
        Object.entries(params).map(([key, value]) => [key, Array.isArray(value) ? value.join(",") : value]),
    );
