package com.datathon.api.organization;

import com.datathon.api.common.persistence.DatabaseCode;
import com.datathon.api.common.persistence.DatabaseCodeConverter;
import jakarta.persistence.Converter;

public enum OrganizationType implements DatabaseCode {
    LOCAL_GOVERNMENT("jls"),
    COUNTY("zupanija"),
    PUBLIC_BODY("javno_tijelo"),
    LLC("doo"),
    SIMPLE_LLC("jdoo"),
    JOINT_STOCK("dd"),
    CRAFT("obrt"),
    ASSOCIATION("udruga"),
    NATURAL_PERSON("fizicka_osoba"),
    FOREIGN("strana"),
    OTHER("ostalo"),
    UNKNOWN("nepoznato");

    private final String code;

    OrganizationType(String code) {
        this.code = code;
    }

    @Override
    public String code() {
        return code;
    }

    @Converter(autoApply = true)
    public static class JpaConverter extends DatabaseCodeConverter<OrganizationType> {
        public JpaConverter() {
            super(OrganizationType.class);
        }
    }
}
