package com.datathon.api.wording;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import org.springframework.core.io.ClassPathResource;

/**
 * The forbidden wording list from {@code wording/forbidden_terms_hr.txt}. Used by the wording test on all templates
 * and, later, by the AI assessment validator (docs/ANOMALY_DETECTION.md §4.4). No exception for negations.
 */
public final class ForbiddenTerms {

    public static final String RESOURCE = "wording/forbidden_terms_hr.txt";

    private static final List<String> TERMS = load();

    private ForbiddenTerms() {
    }

    public static List<String> terms() {
        return TERMS;
    }

    /** @return the forbidden stems found in {@code text}, empty when the text is clean */
    public static List<String> findIn(String text) {
        if (text == null) {
            return List.of();
        }
        String lower = text.toLowerCase(Locale.ROOT);
        return TERMS.stream().filter(lower::contains).toList();
    }

    private static List<String> load() {
        try (InputStream in = new ClassPathResource(RESOURCE).getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).lines()
                    .map(String::strip)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .map(line -> line.toLowerCase(Locale.ROOT))
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
