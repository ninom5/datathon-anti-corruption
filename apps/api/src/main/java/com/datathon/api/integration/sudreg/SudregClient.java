package com.datathon.api.integration.sudreg;

import java.util.Optional;

/**
 * Sudski registar (sudreg-data.gov.hr). Implementations: {@link RestSudregClient} (OAuth2, needs SUDREG_CLIENT_ID and
 * SUDREG_CLIENT_SECRET) and {@link NoopSudregClient} without credentials. Only companies and public bodies are looked
 * up; crafts and natural persons are not in the register and their OIBs are never sent (GDPR).
 */
public interface SudregClient {

    boolean isConfigured();

    /**
     * @return empty when the register has no subject with this OIB
     * @throws com.datathon.api.common.ExternalSourceException the register cannot be reached; the message never
     *         carries the OIB or the credentials
     */
    Optional<SudregCompany> findByOib(String oib);
}
