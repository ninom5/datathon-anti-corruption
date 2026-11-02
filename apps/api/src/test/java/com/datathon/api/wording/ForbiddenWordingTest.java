package com.datathon.api.wording;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * AGENTS.md: no forbidden term may appear in code, templates or resources that can reach a user. Scans the backend
 * sources and resources and, when present, the frontend sources. The list itself is the only excluded file.
 */
class ForbiddenWordingTest {

    private static final List<Path> ROOTS = List.of(Path.of("src/main"), Path.of("../web/src"));
    private static final List<String> TEXT_EXTENSIONS = List.of(".java", ".yaml", ".yml", ".properties", ".sql", ".csv",
            ".txt", ".json", ".html", ".ts", ".tsx", ".css", ".md");

    @Test
    void negationIsNotAnException() {
        assertThat(ForbiddenTerms.findIn("Ovo ne znači korupciju.")).containsExactly("korupcij");
        assertThat(ForbiddenTerms.findIn("Ovo NIJE NEZAKONITO.")).containsExactly("nezakonit");
        assertThat(ForbiddenTerms.findIn("Povećanje može biti predviđeno ugovorom.")).isEmpty();
    }

    @Test
    void sourcesAndResourcesContainNoForbiddenTerms() throws IOException {
        List<String> hits = new ArrayList<>();
        for (Path root : ROOTS) {
            if (Files.isDirectory(root)) {
                hits.addAll(scan(root));
            }
        }
        assertThat(hits).as("Zabranjene riječi (AGENTS.md) u izvornom kodu ili resursima").isEmpty();
    }

    private static List<String> scan(Path root) throws IOException {
        List<String> hits = new ArrayList<>();
        try (Stream<Path> files = Files.walk(root)) {
            for (Path file : files.filter(ForbiddenWordingTest::isScanned).toList()) {
                List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
                for (int index = 0; index < lines.size(); index++) {
                    for (String term : ForbiddenTerms.findIn(lines.get(index))) {
                        hits.add(file + ":" + (index + 1) + " -> " + term);
                    }
                }
            }
        }
        return hits;
    }

    private static boolean isScanned(Path file) {
        String name = file.getFileName().toString();
        return Files.isRegularFile(file) && !file.endsWith(Path.of(ForbiddenTerms.RESOURCE))
                && TEXT_EXTENSIONS.stream().anyMatch(name::endsWith);
    }
}
