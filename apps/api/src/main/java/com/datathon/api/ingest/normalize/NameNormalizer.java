package com.datathon.api.ingest.normalize;

import java.util.Locale;
import java.util.regex.Pattern;

/** {@code name_norm} from docs/DATA_MODEL.md §3, used to match organizations that have no OIB. */
public final class NameNormalizer {

    private static final Pattern SIMPLE_LLC = Pattern.compile("\\bj\\.\\s*d\\.\\s*o\\.\\s*o\\.?");
    /** Also matches a cut-off "d.o" at the end of the name, seen in real registers. */
    private static final Pattern LLC = Pattern.compile("\\bd\\.\\s*o\\.\\s*o\\.?|\\bd\\.\\s*o\\.?$|drustvo s ogranicenom odgovornoscu");
    private static final Pattern JOINT_STOCK = Pattern.compile("\\bd\\.\\s*d\\.?(?=[\\s,;]|$)");
    private static final Pattern PUNCTUATION = Pattern.compile("[^\\p{L}\\p{N}\\s]");
    private static final Pattern SPACES = Pattern.compile("\\s+");

    private NameNormalizer() {
    }

    public static String normalize(String name) {
        if (name == null) {
            return "";
        }
        String text = stripDiacritics(name.toLowerCase(Locale.ROOT));
        text = SIMPLE_LLC.matcher(text).replaceAll(" jdoo ");
        text = LLC.matcher(text).replaceAll(" doo ");
        text = JOINT_STOCK.matcher(text).replaceAll(" dd ");
        text = PUNCTUATION.matcher(text).replaceAll(" ");
        return SPACES.matcher(text).replaceAll(" ").strip();
    }

    private static String stripDiacritics(String text) {
        return text.replace("č", "c").replace("ć", "c").replace("š", "s").replace("ž", "z").replace("đ", "dj");
    }
}
