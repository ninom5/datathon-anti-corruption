package com.datathon.api.indicator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.datathon.api.common.PageResponse;
import com.datathon.api.common.NotFoundException;
import com.datathon.api.indicator.dto.IndicatorContract;
import com.datathon.api.indicator.dto.IndicatorDetail;
import com.datathon.api.indicator.dto.IndicatorFilter;
import com.datathon.api.indicator.dto.IndicatorResponse;
import com.datathon.api.indicator.dto.IndicatorSubject;
import com.datathon.api.organization.dto.OrganizationRef;
import com.datathon.api.rules.EvidenceLink;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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

    @Test
    void detailReturnsFlatIndicatorWithContractsAndSubject() {
        IndicatorResponse splitting = new IndicatorResponse(7L, "F_SPLITTING", "rules-v1", "Moguće cjepkanje", "group",
                new OrganizationRef(10L, "00000000003", "Općina Uzorkovo", "jls", false, true), List.of(101L, 102L),
                new IndicatorResponse.Magnitude(new BigDecimal("2.4864"), "omjer", "zbroj je 2,49 × prag", "visok"),
                "visoka", "jaka", "vrijedi_provjeriti", false, null, "Između ...", "Ugovori mogu ...", List.of("plan nabave"),
                List.of(), Instant.parse("2026-11-03T18:00:00Z"), true);
        IndicatorContract contract = new IndicatorContract(101L, LocalDate.parse("2025-02-03"), "Dječje igralište", "37535200",
                new OrganizationRef(20L, "00000000011", "Igra d.o.o.", "doo", false, true), new BigDecimal("24800.00"),
                new BigDecimal("24800.00"), "Jednostavna nabava");
        IndicatorSubject subject = new IndicatorSubject("375", 3, LocalDate.parse("2025-02-03"), LocalDate.parse("2025-04-28"),
                new BigDecimal("26540.00"), "00000000011", null, null, null);
        given(indicatorService.getById(7L)).willReturn(new IndicatorDetail(splitting, List.of(contract), subject));

        assertThat(mvc.get().uri("/api/v1/indicators/7"))
                .hasStatusOk()
                .bodyJson()
                .satisfies(json -> {
                    assertThat(json).extractingPath("$.id").isEqualTo(7);
                    assertThat(json).extractingPath("$.ruleCode").isEqualTo("F_SPLITTING");
                    assertThat(json).extractingPath("$.magnitude.band").isEqualTo("visok");
                    assertThat(json).extractingPath("$.contracts[0].supplier.name").isEqualTo("Igra d.o.o.");
                    assertThat(json).extractingPath("$.subject.threshold").isEqualTo(26540.00);
                    assertThat(json).extractingPath("$.subject.from").isEqualTo("2025-02-03");
                    assertThat(json).doesNotHavePath("$.subject.cpv2");
                    assertThat(json).doesNotHavePath("$.indicator");
                });
    }

    @Test
    void missingIndicatorIs404AndInvalidIdIs400() {
        given(indicatorService.getById(999L)).willThrow(new NotFoundException("Indikator 999 ne postoji"));

        assertThat(mvc.get().uri("/api/v1/indicators/999"))
                .hasStatus(404)
                .bodyJson().extractingPath("$.message").isEqualTo("Indikator 999 ne postoji");
        assertThat(mvc.get().uri("/api/v1/indicators/abc")).hasStatus(400);
    }
}
