package com.datathon.api.assessment;

import com.datathon.api.organization.OrganizationType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import tools.jackson.databind.json.JsonMapper;
import java.util.Set;

/**
 * What the model sees about one contract (docs/ANOMALY_DETECTION.md §4.2), only data from our database.
 *
 * @param data          the context as nested sorted maps
 * @param canonicalJson {@code data} with sorted keys; the exact text sent to the model and hashed
 * @param fieldPaths    every path the model may cite ({@code ugovor.predmet}, {@code pravila[0].magnitude},
 *                      {@code pravila.magnitude}), also keys whose value is null ("nije objavljeno")
 * @param sourceUrls    every source URL in the context; a reason may cite only these
 * @param supplierType  for the validator's rule on crafts and natural persons (never sent separately to the model)
 */
public record AiContext(Long contractId, Map<String, Object> data, String canonicalJson, Set<String> fieldPaths,
                        Set<String> sourceUrls, OrganizationType supplierType, String supplierName) {

    private static final JsonMapper JSON = JsonMapper.shared();

    /** A context from ready data (e.g. a synthetic evaluation case); keys are sorted so the JSON is canonical. */
    public static AiContext of(Long contractId, Map<String, Object> data, OrganizationType supplierType,
                               String supplierName) {
        @SuppressWarnings("unchecked")
        Map<String, Object> sorted = (Map<String, Object>) sortKeys(data);
        return new AiContext(contractId, sorted, JSON.writeValueAsString(sorted), ContextPaths.fieldPaths(sorted),
                ContextPaths.sourceUrls(sorted), supplierType, supplierName);
    }

    private static Object sortKeys(Object node) {
        if (node instanceof Map<?, ?> map) {
            Map<String, Object> sorted = new TreeMap<>();
            map.forEach((key, value) -> sorted.put(key.toString(), sortKeys(value)));
            return sorted;
        }
        if (node instanceof List<?> list) {
            List<Object> copy = new ArrayList<>();
            list.forEach(element -> copy.add(sortKeys(element)));
            return copy;
        }
        return node;
    }
}
