package com.datathon.api.common.persistence;

import jakarta.persistence.AttributeConverter;
import java.util.Arrays;

public abstract class DatabaseCodeConverter<E extends Enum<E> & DatabaseCode> implements AttributeConverter<E, String> {

    private final Class<E> type;

    protected DatabaseCodeConverter(Class<E> type) {
        this.type = type;
    }

    @Override
    public String convertToDatabaseColumn(E value) {
        return value == null ? null : value.code();
    }

    @Override
    public E convertToEntityAttribute(String code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(type.getEnumConstants())
                .filter(constant -> constant.code().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unknown " + type.getSimpleName() + " code: " + code));
    }
}
