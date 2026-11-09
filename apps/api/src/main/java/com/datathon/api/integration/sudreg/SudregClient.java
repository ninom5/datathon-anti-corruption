package com.datathon.api.integration.sudreg;

import java.util.Optional;

public interface SudregClient {

    boolean isConfigured();

    Optional<SudregCompany> findByOib(String oib);
}
