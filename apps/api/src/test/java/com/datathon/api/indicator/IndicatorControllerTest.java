package com.datathon.api.indicator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.datathon.api.common.PageResponse;
import com.datathon.api.indicator.dto.IndicatorFilter;
import com.datathon.api.indicator.dto.IndicatorResponse;
import com.datathon.api.organization.dto.OrganizationRef;
import com.datathon.api.rules.EvidenceLink;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(IndicatorController.class)
class IndicatorControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private IndicatorService indicatorService;

    @Test
    void listReturnsGroupIndicatorWithContractsAndBand() {
        IndicatorResponse splitting = new IndicatorResponse(7L, "F_SPLITTING", "rules-v1",
                "Više manjih ugovora s istim dobavljačem (moguće cjepkanje)", "group",
                new OrganizationRef(10L, "00000000003", "Općina Uzorkovo", "jls", false, true), List.of(101L, 102L, 103L),
                new IndicatorResponse.Magnitude(new BigDecimal("2.4864"), "omjer", "zbroj je 2,49 × prag jednostavne nabave", "visok"),
                "visoka", "jaka", "vrijedi_provjeriti", false, null, "Između 03.02.2025. i 28.04.2025. ...",
                "Ugovori mogu odgovarati odvojenim potrebama.", List.of("plan nabave"),
                List.of(new EvidenceLink("Registar ugovora, list 1, redak 3", "https://primjerovo.invalid/r.xlsx", "Iznos bez PDV-a")),
                Instant.parse("2026-11-03T18:00:00Z"), true);
        given(indicatorService.find(any())).willReturn(new PageResponse<>(List.of(splitting), 1, 0, 20));

        assertThat(mvc.get().uri("/api/v1/indicators?ruleCode=F_SPLITTING,C_CONCENTRATION&buyerOib=00000000003&band=visok&synthetic=true"))
                .hasStatusOk()
                .bodyJson()
                .satisfies(json -> {
                    assertThat(json).extractingPath("$.totalElements").isEqualTo(1);
                    assertThat(json).extractingPath("$.content[0].subjectType").isEqualTo("group");
                    assertThat(json).extractingPath("$.content[0].contractIds").asArray().hasSize(3);
                    assertThat(json).extractingPath("$.content[0].magnitude.band").isEqualTo("visok");
                    assertThat(json).extractingPath("$.content[0].evidenceStrength").isEqualTo("jaka");
                    assertThat(json).extractingPath("$.content[0].buyer.oib").isEqualTo("00000000003");
                    assertThat(json).extractingPath("$.content[0].synthetic").isEqualTo(true);
                });

        ArgumentCaptor<IndicatorFilter> filter = ArgumentCaptor.forClass(IndicatorFilter.class);
        Mockito.verify(indicatorService).find(filter.capture());
        assertThat(filter.getValue().ruleCodes()).containsExactly("F_SPLITTING", "C_CONCENTRATION");
        assertThat(filter.getValue().band()).isEqualTo("visok");
        assertThat(filter.getValue().includesSynthetic()).isTrue();
    }

    @Test
    void invalidFiltersAreRejectedWithCroatianMessage() {
        assertThat(mvc.get().uri("/api/v1/indicators?ruleCode=X_UNKNOWN"))
                .hasStatus(400)
                .bodyJson().extractingPath("$.message").asString().contains("dopuštene vrijednosti");
        assertThat(mvc.get().uri("/api/v1/indicators?band=ogroman")).hasStatus(400);
        assertThat(mvc.get().uri("/api/v1/indicators?buyerOib=12")).hasStatus(400);
    }
}
