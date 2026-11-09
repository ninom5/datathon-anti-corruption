package com.datathon.api.wording;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import org.springframework.core.io.ClassPathResource;

public final class ForbiddenTerms {

    public static final String RESOURCE = "wording/forbidden_terms_hr.txt";

    private static final List<String> TERMS = load();

    private ForbiddenTerms() {
    }

    public static List<String> terms() {
        return TERMS;
    }

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
