package com.datathon.api.common.persistence;

import java.util.Arrays;
import java.util.Optional;

/** An enum stored in the database under the code from docs/DATA_MODEL.md (e.g. LLC is stored as "doo"). */
public interface DatabaseCode {

    String code();

    /** The constant stored or sent as {@code code}; empty for an unknown code, so input validation can say so. */
    static <E extends Enum<E> & DatabaseCode> Optional<E> fromCode(Class<E> type, String code) {
        return Arrays.stream(type.getEnumConstants()).filter(constant -> constant.code().equals(code)).findFirst();
    }
}
