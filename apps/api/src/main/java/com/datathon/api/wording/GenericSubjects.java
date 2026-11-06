package com.datathon.api.wording;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.springframework.core.io.ClassPathResource;

/**
 * Generic contract subjects from {@code wording/generic_subjects_hr.txt} ("nejasan predmet",
 * docs/ANOMALY_DETECTION.md §4.1). Used only to choose which contracts get an AI assessment.
 */
public final class GenericSubjects {

    public static final String RESOURCE = "wording/generic_subjects_hr.txt";

    private static final List<String> PHRASES = load();

    private GenericSubjects() {
    }

    /** A subject is vague when it has fewer than {@code minWords} words or contains a generic phrase. */
    public static boolean isVague(String subject, int minWords) {
        if (subject == null || subject.isBlank()) {
            return true;
        }
        String lower = subject.toLowerCase(Locale.ROOT);
        long words = Arrays.stream(lower.split("[^\\p{L}\\p{N}]+")).filter(word -> !word.isEmpty()).count();
        return words < minWords || PHRASES.stream().anyMatch(lower::contains);
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
