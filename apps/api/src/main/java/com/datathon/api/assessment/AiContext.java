package com.datathon.api.assessment;

import com.datathon.api.organization.OrganizationType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import tools.jackson.databind.json.JsonMapper;
import java.util.Set;

public record AiContext(Long contractId, Map<String, Object> data, String canonicalJson, Set<String> fieldPaths,
                        Set<String> sourceUrls, OrganizationType supplierType, String supplierName) {

    private static final JsonMapper JSON = JsonMapper.shared();

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
