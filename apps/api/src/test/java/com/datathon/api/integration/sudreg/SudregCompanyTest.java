package com.datathon.api.integration.sudreg;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Parsing of a real (trimmed) answer of /api/javni/detalji_subjekta, saved 2026-11-04. */
class SudregCompanyTest {

    private static JsonNode fixture() throws IOException {
        try (InputStream in = new ClassPathResource("fixtures/sudreg/detalji-subjekta-doo.json").getInputStream()) {
            return JsonMapper.shared().readTree(in);
        }
    }

    @Test
    void readsNkdFromPretezitaDjelatnostAndFoundingDate() throws IOException {
        SudregCompany company = SudregCompany.from(fixture());

        assertThat(company.oib()).isEqualTo("52860656310");
        assertThat(company.foundedOn()).isEqualTo(LocalDate.of(2020, 12, 9));
        assertThat(company.nkdCode()).isEqualTo("41.00.0");
        assertThat(company.nkdName()).isEqualTo("Građenje stambenih i nestambenih zgrada");
        assertThat(company.nkdVersion()).isEqualTo("NKD 2025");
        assertThat(company.legalForm()).isEqualTo("d.o.o.");
        assertThat(company.inBankruptcy()).isFalse();
        assertThat(company.inLiquidation()).isFalse();
        assertThat(company.reducedPayload()).containsKeys("oib", "nkd", "datum_osnivanja")
                .doesNotContainKeys("email_adrese", "sjediste");
    }

    @Test
    void oibSentAsNumberKeepsItsLeadingZero() {
        JsonNode answer = JsonMapper.shared().readTree("{\"oib\": 1234567890, \"tvrtka\": {\"ime\": \"Primjer d.o.o.\"},"
                + " \"postupak\": {\"postupak\": {\"znacenje\": \"Otvoren stečajni postupak\"}}}");

        SudregCompany company = SudregCompany.from(answer);

        assertThat(company.oib()).isEqualTo("01234567890");
        assertThat(company.inBankruptcy()).isTrue();
        assertThat(company.foundedOn()).isNull();
    }
}
