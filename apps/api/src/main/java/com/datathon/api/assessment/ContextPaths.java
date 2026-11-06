package com.datathon.api.assessment;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/** Citable field paths and source URLs of a context (docs/ANOMALY_DETECTION.md §4.4: the validator uses both). */
final class ContextPaths {

    /** Keys whose values are source links; elements of {@code izvori} lists are links as well. */
    private static final Set<String> URL_KEYS = Set.of("izvor_url", "eojn_url", "izvori");

    private ContextPaths() {
    }

    static Set<String> fieldPaths(Map<String, Object> data) {
        Set<String> paths = new TreeSet<>();
        collectPaths(data, "", paths);
        return paths;
    }

    static Set<String> sourceUrls(Map<String, Object> data) {
        Set<String> urls = new TreeSet<>();
        collectUrls(data, null, urls);
        return urls;
    }

    /**
     * Every key is citable, also when its value is null: a reason may say that something is not published
     * (§4.4: the field must exist in the context). List elements are citable with an index ({@code pravila[0].x}) and
     * without one ({@code pravila.x}).
     */
    private static void collectPaths(Object node, String path, Set<String> paths) {
        if (!path.isEmpty()) {
            paths.add(path);
        }
        if (node instanceof Map<?, ?> map) {
            map.forEach((key, value) -> collectPaths(value, path.isEmpty() ? key.toString() : path + "." + key, paths));
        } else if (node instanceof List<?> list) {
            for (int index = 0; index < list.size(); index++) {
                collectPaths(list.get(index), path + "[" + index + "]", paths);
                collectPaths(list.get(index), path, paths);
            }
        }
    }

    private static void collectUrls(Object node, String key, Set<String> urls) {
        if (node instanceof Map<?, ?> map) {
            map.forEach((childKey, value) -> collectUrls(value, childKey.toString(), urls));
        } else if (node instanceof Collection<?> list) {
            list.forEach(element -> collectUrls(element, key, urls));
        } else if (node instanceof String text && key != null && URL_KEYS.contains(key) && !text.isBlank()) {
            urls.add(text);
        }
    }
}
