package com.datathon.api.source.dto;

import com.datathon.api.reference.ReferenceSources;
import com.datathon.api.source.SourceFile;
import java.time.Instant;

public record SourceFileResponse(Long id, String name, String url, String format, String formatClass, String status,
                                 Integer httpStatus, Instant retrievedAt, long rows, long contracts) {

    public static SourceFileResponse from(SourceFile file, long rows, long contracts) {
        return new SourceFileResponse(file.getId(), file.getName(), file.getUrl(), file.getFormat(),
                file.getFormatClass() == null ? null : file.getFormatClass().name(), statusOf(file),
                file.getHttpStatus(), file.getRetrievedAt(), rows, contracts);
    }

    public static String statusOf(SourceFile file) {
        if (file.getErrorMessage() != null) {
            return Integer.valueOf(200).equals(file.getHttpStatus()) ? "necitljivo" : "nedostupno";
        }
        if (file.getFormatClass() == null) {
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
