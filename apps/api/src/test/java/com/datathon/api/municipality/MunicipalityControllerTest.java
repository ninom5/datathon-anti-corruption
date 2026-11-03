package com.datathon.api.municipality;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.datathon.api.common.NotFoundException;
import com.datathon.api.municipality.dto.MunicipalityOverview;
import com.datathon.api.municipality.dto.PeerComparison;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(MunicipalityController.class)
class MunicipalityControllerTest {

    private static final String OIB = "84121580205";

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private MunicipalityService municipalityService;

    @Test
    void listKeepsJlsWithoutDataAsOwnCategory() {
        given(municipalityService.listAll()).willReturn(List.of(
                new MunicipalityOverview(OIB, "Ivanec", "grad", "VARAŽDINSKA", 12000, "P5", 200, 2018, 2026, 3, 1,
                        new BigDecimal("1.5")),
                new MunicipalityOverview("12345678901", "Bednja", "opcina", "VARAŽDINSKA", 3500, "P2", 0, null, null,
                        0, 0, null)));

        assertThat(mvc.get().uri("/api/v1/municipalities"))
                .hasStatusOk()
                .bodyJson()
                .satisfies(json -> {
                    assertThat(json).extractingPath("$[0].worthCheckingPer100").isEqualTo(1.5);
                    assertThat(json).extractingPath("$[1].contracts").isEqualTo(0);
                    assertThat(json).extractingPath("$[1].worthCheckingPer100").isNull();
                });
    }

    @Test
    void peersReturnsGroupMedianAndRank() {
        PeerComparison comparison = new PeerComparison(
                new PeerComparison.Municipality(OIB, "Ivanec", "grad", "VARAŽDINSKA", 12000),
                "P5", "gradovi od 10.000 do 49.999 stanovnika", 2025, false,
                List.of(new PeerComparison.YearOption(2025, 9, false)),
                "45", "Građevinski radovi",
                List.of(new PeerComparison.CategoryOption("45", "Građevinski radovi", 9, 120, new BigDecimal("900000.00"))),
                50, 9, 8, true, new BigDecimal("41.20"), 2,
                List.of(new PeerComparison.Member(OIB, "Ivanec", 12000, 40, 12, new BigDecimal("600000.00"),
                        new BigDecimal("50.00"), true)));
        given(municipalityService.comparePeers(OIB, 2025, "45")).willReturn(comparison);

        assertThat(mvc.get().uri("/api/v1/municipalities/" + OIB + "/peers?year=2025&cpv2=45"))
                .hasStatusOk()
                .bodyJson()
                .satisfies(json -> {
                    assertThat(json).extractingPath("$.sufficient").isEqualTo(true);
                    assertThat(json).extractingPath("$.medianPerCapita").isEqualTo(41.2);
                    assertThat(json).extractingPath("$.selectedRank").isEqualTo(2);
                    assertThat(json).extractingPath("$.members[0].selected").isEqualTo(true);
                });
    }

    @Test
    void peersRejectsInvalidCpvDivision() {
        assertThat(mvc.get().uri("/api/v1/municipalities/" + OIB + "/peers?cpv2=4")).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void peersReturns404ForUnknownJls() {
        given(municipalityService.comparePeers(OIB, null, null)).willThrow(new NotFoundException("JLS ne postoji"));

        assertThat(mvc.get().uri("/api/v1/municipalities/" + OIB + "/peers")).hasStatus(HttpStatus.NOT_FOUND);
    }
}
