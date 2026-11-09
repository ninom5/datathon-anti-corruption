export const toQueryParams = <T extends object>(params: T): Record<string, unknown> =>
    Object.fromEntries(
        Object.entries(params).map(([key, value]) => [key, Array.isArray(value) ? value.join(",") : value]),
    );
