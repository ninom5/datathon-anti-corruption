package com.datathon.api.ingest.http;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DiskCache {

    private final Path root;

    public DiskCache(Path root) {
        this.root = root;
    }

    public Optional<byte[]> read(URI uri, String extension, Duration maxAge) {
        Path path = pathFor(uri, extension);
        try {
            if (!Files.exists(path) || isExpired(path, maxAge)) {
                return Optional.empty();
            }
            return Optional.of(Files.readAllBytes(path));
        } catch (IOException e) {
            log.warn("Cannot read cache file {}: {}", path, e.getMessage());
            return Optional.empty();
        }
    }

    public void write(URI uri, String extension, byte[] body) {
        Path path = pathFor(uri, extension);
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, body);
        } catch (IOException e) {
            log.warn("Cannot write cache file {}: {}", path, e.getMessage());
        }
    }

    /** When the cached copy was written, i.e. when it was really downloaded. */
    public Optional<Instant> lastModified(URI uri, String extension) {
        Path path = pathFor(uri, extension);
        try {
            return Files.exists(path) ? Optional.of(Files.getLastModifiedTime(path).toInstant()) : Optional.empty();
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private boolean isExpired(Path path, Duration maxAge) throws IOException {
        if (maxAge == null) {
            return false;
        }
        Instant modified = Files.getLastModifiedTime(path).toInstant();
        return modified.plus(maxAge).isBefore(Instant.now());
    }

    private Path pathFor(URI uri, String extension) {
        String host = uri.getAuthority().replace(':', '_');
        return root.resolve(host).resolve(sha1(uri.toString()) + "." + extension);
    }

    private static String sha1(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-1").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
