package com.datathon.api.integration.sudreg;

import com.datathon.api.common.ExternalSourceException;
import java.util.Optional;

/** Without credentials nothing is looked up; the AI context then keeps NKD and founding date as "nedostupno". */
public class NoopSudregClient implements SudregClient {

    @Override
    public boolean isConfigured() {
        return false;
    }

    @Override
    public Optional<SudregCompany> findByOib(String oib) {
        throw new ExternalSourceException("Sudreg", "credentials are not set");
    }
}
