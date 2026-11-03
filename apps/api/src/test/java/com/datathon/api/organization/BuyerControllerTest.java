package com.datathon.api.organization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.datathon.api.common.NotFoundException;
import com.datathon.api.organization.dto.BuyerProfile;
import com.datathon.api.organization.dto.OrganizationRef;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(BuyerController.class)
class BuyerControllerTest {

    private static final String OIB = "84121580205";

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private BuyerService buyerService;
    @MockitoBean
    private BuyerProfileService buyerProfileService;

    @Test
    void profileReturnsCoverageMunicipalityAndCounts() {
        BuyerProfile profile = new BuyerProfile(
                new OrganizationRef(1L, OIB, "GRAD IVANEC", "jls", false, false),
                new BuyerProfile.Jpt("1234-5", "Jedinica lokalne i područne (regionalne) samouprave - Grad", "VARAŽDINSKA ŽUPANIJA"),
                new BuyerProfile.MunicipalityInfo("Ivanec", "grad", "VARAŽDINSKA ŽUPANIJA", 12000, "P2"),
                new BuyerProfile.Coverage(2018, 2026, List.of(2018, 2025, 2026), List.of(2026),
                        "2018.–2026., bez ugovora u nekim godinama, 2026. nepotpuna"),
                3, new BigDecimal("150000.00"),
                List.of(new BuyerProfile.YearTotal(2026, 1, new BigDecimal("50000.00"), true)),
                List.of(new BuyerProfile.CpvTotal("45", 2, new BigDecimal("100000.00"))),
                List.of(new BuyerProfile.SupplierTotal(new OrganizationRef(2L, "00000000012", "Primjer d.o.o.", "doo", false, false),
                        2, new BigDecimal("100000.00"))),
                1,
                List.of(new BuyerProfile.RuleCount("F_SPLITTING", "Više manjih ugovora s istim dobavljačem (moguće cjepkanje)", 2, 2, 0)),
                List.of(),
                List.of(new BuyerProfile.SourceRef("Registar ugovora Grada Ivanca", "https://data.gov.hr/ckan/dataset/x", "A2", 4, 3)),
                false);
        given(buyerProfileService.getByOib(OIB)).willReturn(profile);

        assertThat(mvc.get().uri("/api/v1/buyers/" + OIB))
                .hasStatusOk()
                .bodyJson()
                .satisfies(json -> {
                    assertThat(json).extractingPath("$.coverage.label").isEqualTo("2018.–2026., bez ugovora u nekim godinama, 2026. nepotpuna");
                    assertThat(json).extractingPath("$.municipality.peerGroup").isEqualTo("P2");
                    assertThat(json).extractingPath("$.municipality.population2021").isEqualTo(12000);
                    assertThat(json).extractingPath("$.jpt.county").isEqualTo("VARAŽDINSKA ŽUPANIJA");
                    assertThat(json).extractingPath("$.byYear[0].incomplete").isEqualTo(true);
                    assertThat(json).extractingPath("$.craftContracts").isEqualTo(1);
                    assertThat(json).extractingPath("$.indicatorCounts[0].worthChecking").isEqualTo(2);
                    assertThat(json).extractingPath("$.sources[0].formatClass").isEqualTo("A2");
                });
    }

    @Test
    void unknownBuyerIs404AndInvalidOibIs400() {
        given(buyerProfileService.getByOib("00000000099")).willThrow(new NotFoundException("Naručitelj s OIB-om 00000000099 ne postoji u podacima."));

        assertThat(mvc.get().uri("/api/v1/buyers/00000000099"))
                .hasStatus(404)
                .bodyJson().extractingPath("$.message").isEqualTo("Naručitelj s OIB-om 00000000099 ne postoji u podacima.");
        assertThat(mvc.get().uri("/api/v1/buyers/123")).hasStatus(400);
    }
}
