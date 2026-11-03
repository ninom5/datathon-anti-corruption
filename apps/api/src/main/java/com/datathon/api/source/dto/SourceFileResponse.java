package com.datathon.api.source.dto;

import com.datathon.api.reference.ReferenceSources;
import com.datathon.api.source.SourceFile;
import java.time.Instant;

/**
 * One downloaded file of a dataset and what happened to it.
 *
 * @param status    {@code uvezeno} (class A or A2 parsed, or a reference source: JPT, DZS), {@code format_nije_podrzan} (class B or C, later),
 *                  {@code nestrukturirano} (class D), {@code necitljivo} (downloaded but not readable) or
 *                  {@code nedostupno} (download failed)
 * @param rows      rows read from the file (raw records)
 * @param contracts contracts whose current version comes from this file (the same contract can appear in several
 *                  yearly files; the newest file wins)
 */
public record SourceFileResponse(Long id, String name, String url, String format, String formatClass, String status,
                                 Integer httpStatus, Instant retrievedAt, long rows, long contracts) {

    public static SourceFileResponse from(SourceFile file, long rows, long contracts) {
        return new SourceFileResponse(file.getId(), file.getName(), file.getUrl(), file.getFormat(),
                file.getFormatClass() == null ? null : file.getFormatClass().name(), statusOf(file),
                file.getHttpStatus(), file.getRetrievedAt(), rows, contracts);
    }

    /** Error messages are technical and may quote foreign URLs, so only a status code is exposed. */
    public static String statusOf(SourceFile file) {
        if (file.getErrorMessage() != null) {
            return Integer.valueOf(200).equals(file.getHttpStatus()) ? "necitljivo" : "nedostupno";
        }
        if (file.getFormatClass() == null) {
            // Reference sources (JPT JSON, DZS census) are not contract registers and have no format class.
            return isReferenceFile(file) ? "uvezeno" : "necitljivo";
        }
        return switch (file.getFormatClass()) {
            case A, A2 -> "uvezeno";
            case B, C -> "format_nije_podrzan";
            case D -> "nestrukturirano";
        };
    }

    private static boolean isReferenceFile(SourceFile file) {
        String portal = file.getDataset().getPortal();
        return ReferenceSources.PORTAL_JPT.equals(portal) || ReferenceSources.PORTAL_DZS.equals(portal);
    }

}
