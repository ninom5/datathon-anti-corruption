package com.datathon.api.common.persistence;

/** An enum stored in the database under the code from docs/DATA_MODEL.md (e.g. LLC is stored as "doo"). */
public interface DatabaseCode {

    String code();
}
