package com.datathon.api.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.datathon.api.common.NotFoundException;
import com.datathon.api.common.PageResponse;
import com.datathon.api.contract.dto.ContractFilter;
import com.datathon.api.contract.dto.ContractListItem;
import com.datathon.api.contract.dto.ContractSummary;
import com.datathon.api.indicator.dto.IndicatorBadge;
import com.datathon.api.organization.dto.OrganizationRef;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(ContractController.class)
class ContractControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private ContractService contractService;

    @Test
    void listReturnsPageWithFiltersBound() {
        ContractListItem item = new ContractListItem(1L,
                new OrganizationRef(10L, "00000000001", "Općina Primjerovo", "jls", false, true),
                new OrganizationRef(11L, "00000000011", OrganizationRef.HIDDEN_CRAFT_NAME, "obrt", true, true),
                "Rekonstrukcija ceste", "radovi", LocalDate.of(2025, 3, 1), 2025, false,
                new BigDecimal("200000"), new BigDecimal("350000"), null,
                List.of(new IndicatorBadge(5L, "D_VALUE_INCREASE", new BigDecimal("0.75"), "+75 %", "vrijedi_provjeriti", false)),
                true);
        given(contractService.find(any())).willReturn(new PageResponse<>(List.of(item), 1, 0, 20));

        assertThat(mvc.get().uri("/api/v1/contracts?buyerOib=00000000001&year=2025&severity=informativno&synthetic=true"))
                .hasStatusOk()
                .bodyJson()
                .satisfies(json -> {
                    assertThat(json).extractingPath("$.totalElements").isEqualTo(1);
                    assertThat(json).extractingPath("$.content[0].supplier.name").isEqualTo("Obrt (naziv skriven)");
                    assertThat(json).extractingPath("$.content[0].indicators[0].magnitudeLabel").isEqualTo("+75 %");
                    assertThat(json).extractingPath("$.content[0].synthetic").isEqualTo(true);
                });

        ArgumentCaptor<ContractFilter> filter = ArgumentCaptor.forClass(ContractFilter.class);
        Mockito.verify(contractService).find(filter.capture());
        assertThat(filter.getValue().buyerOib()).isEqualTo("00000000001");
        assertThat(filter.getValue().year()).isEqualTo(2025);
        assertThat(filter.getValue().severity()).isEqualTo("informativno");
        assertThat(filter.getValue().includesSynthetic()).isTrue();
    }

    @Test
    void summaryReportsPossibleEntryErrors() {
        given(contractService.summarize(any())).willReturn(
                new ContractSummary(100, new BigDecimal("1000000"), 5, 7, 5, 2, 12, List.of(2026), 0));

        assertThat(mvc.get().uri("/api/v1/contracts/summary"))
                .hasStatusOk()
                .bodyJson()
                .satisfies(json -> {
                    assertThat(json).extractingPath("$.possibleEntryErrors").isEqualTo(2);
                    assertThat(json).extractingPath("$.excludedCraftContracts").isEqualTo(12);
                });
    }

    @Test
    void invalidFiltersAreRejectedWithCroatianMessage() {
        assertThat(mvc.get().uri("/api/v1/contracts?buyerOib=123"))
                .hasStatus(400)
                .bodyJson().extractingPath("$.message").isEqualTo("buyerOib: OIB mora imati 11 znamenki");
        assertThat(mvc.get().uri("/api/v1/contracts?severity=sve")).hasStatus(400);
        assertThat(mvc.get().uri("/api/v1/contracts?year=abc")).hasStatus(400);
        assertThat(mvc.get().uri("/api/v1/contracts/abc")).hasStatus(400);
    }

    @Test
    void unknownContractIs404() {
        given(contractService.getById(42L)).willThrow(new NotFoundException("Ugovor 42 ne postoji."));

        assertThat(mvc.get().uri("/api/v1/contracts/42"))
                .hasStatus(404)
                .bodyJson().extractingPath("$.message").isEqualTo("Ugovor 42 ne postoji.");
    }
}
