package com.datathon.api.ingest.normalize;

import com.datathon.api.organization.OrganizationType;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class OrganizationClassifier {

    private static final Pattern NAME_WORD = Pattern.compile("(\\p{Lu}\\p{Ll}+|\\p{Lu}{2,})(-(\\p{Lu}\\p{Ll}+|\\p{Lu}{2,}))?");
    private static final Pattern VOWEL = Pattern.compile("[AEIOUaeiou]");
    private static final List<String> FOREIGN_FORMS = List.of(" gmbh", " ltd", " inc", " s.p.a", " s.r.l", " kft", " sp. z o.o", " ag", " bv", " llc");
    private static final List<String> CRAFT_WORDS = List.of("obrt", " vl.", " vl ", ",vl.", ",vl ", "opg ", "o.p.g",
            "obiteljsko poljoprivredno", "odvjetni", "ured ovlašten", "ured ovlasten", "ured ovl.", " član ", " clan ");
    private static final List<String> INSTITUTION_WORDS = List.of(
            "grad", "općina", "opcina", "županij", "zupanij", "zavod", "ustanova", "društvo", "drustvo", "centar", "dom ",
            "škola", "skola", "vrtić", "vrtic", "klub", "savez", "udruga", "zadruga", "agencija", "fond", "institut",
            "bolnica", "ljekarna", "muzej", "knjižnica", "knjiznica", "hrvatsk", "studio", "servis", "commerce",
            "trade", "projekt", "gradnja", "promet", "ured", "fakultet", "sveučilište", "sveuciliste", "komunal", "vodovod",
            "ministarstvo", "uprava", "policij", "crkva", "župa", "zupa", "tvrtka", "holding", "group", "grupa",
            "kaznionica", "učilište", "uciliste", "gospodarstvo");

    private OrganizationClassifier() {
    }

    public static OrganizationType classifyBuyer(String name) {
        String lower = name == null ? "" : name.toLowerCase(Locale.ROOT);
        if (lower.contains("županij") || lower.contains("zupanij")) {
            return OrganizationType.COUNTY;
        }
        if (lower.startsWith("grad ") || lower.startsWith("općina ") || lower.startsWith("opcina ")) {
            return OrganizationType.LOCAL_GOVERNMENT;
        }
        return OrganizationType.PUBLIC_BODY;
    }

    public static OrganizationType classifySupplier(String name) {
        if (name == null || name.isBlank()) {
            return OrganizationType.UNKNOWN;
        }
        String norm = NameNormalizer.normalize(name);
        String lower = " " + name.toLowerCase(Locale.ROOT).strip();
        if (norm.matches(".*\\bjdoo\\b.*")) {
            return OrganizationType.SIMPLE_LLC;
        }
        if (norm.matches(".*\\bdoo\\b.*")) {
            return OrganizationType.LLC;
        }
        if (norm.matches(".*\\bdd\\b.*")) {
            return OrganizationType.JOINT_STOCK;
        }
        if (CRAFT_WORDS.stream().anyMatch(lower::contains) || looksLikeCraftRegistryName(name)) {
            return OrganizationType.CRAFT;
        }
        if (lower.contains("udruga")) {
            return OrganizationType.ASSOCIATION;
        }
        if (FOREIGN_FORMS.stream().anyMatch(lower::endsWith)) {
            return OrganizationType.FOREIGN;
        }
        if (looksLikePersonName(name)) {
            return OrganizationType.NATURAL_PERSON;
        }
        return OrganizationType.OTHER;
    }

    private static boolean looksLikeCraftRegistryName(String name) {
        return name.chars().filter(character -> character == ',').count() >= 2;
    }

    private static boolean looksLikePersonName(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        if (INSTITUTION_WORDS.stream().anyMatch(lower::contains) || name.chars().anyMatch(Character::isDigit)
                || name.contains("\"")) {
            return false;
        }
        String[] words = name.strip().split("\\s+");
        if (words.length < 2 || words.length > 3) {
            return false;
        }
        for (String word : words) {
            if (!NAME_WORD.matcher(word).matches() || isAcronym(word)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isAcronym(String word) {
        boolean isUpperCase = word.equals(word.toUpperCase(Locale.ROOT));
        return isUpperCase && (word.length() <= 2 || !VOWEL.matcher(word).find());
    }
}
