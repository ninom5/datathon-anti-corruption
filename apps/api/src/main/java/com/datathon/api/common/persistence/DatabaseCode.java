package com.datathon.api.common.persistence;

import java.util.Arrays;
import java.util.Optional;

public interface DatabaseCode {

    String code();

    static <E extends Enum<E> & DatabaseCode> Optional<E> fromCode(Class<E> type, String code) {
        return Arrays.stream(type.getEnumConstants()).filter(constant -> constant.code().equals(code)).findFirst();
    }
}
