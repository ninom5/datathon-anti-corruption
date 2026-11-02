package com.datathon.api.source;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.datathon.api.common.PageResponse;
import com.datathon.api.source.dto.SourceDatasetResponse;
import com.datathon.api.source.dto.SourceFileResponse;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(SourceController.class)
class SourceControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private SourceService sourceService;

    @Test
    void listsDatasetsWithFilesAndRetrievalTime() {
        SourceFileResponse file = new SourceFileResponse(1L, "registar-ugovora.xlsx", "https://data.gov.hr/x.xlsx", "XLSX",
                "A", "uvezeno", 200, Instant.parse("2026-11-02T17:00:00Z"), 120, 100);
        SourceDatasetResponse dataset = new SourceDatasetResponse(9L, "Registar ugovora", "Općina Primjer", "data.gov.hr",
                "https://data.gov.hr/dataset/x", "open-license", null, 100, List.of(file), false);
        given(sourceService.list(false, null, null)).willReturn(new PageResponse<>(List.of(dataset), 1, 0, 20));

        assertThat(mvc.get().uri("/api/v1/sources"))
                .hasStatusOk()
                .bodyJson()
                .satisfies(json -> {
                    assertThat(json).extractingPath("$.content[0].publisher").isEqualTo("Općina Primjer");
                    assertThat(json).extractingPath("$.content[0].files[0].status").isEqualTo("uvezeno");
                    assertThat(json).extractingPath("$.content[0].files[0].retrievedAt").isEqualTo("2026-11-02T17:00:00Z");
                });
    }

    @Test
    void invalidSyntheticFlagIs400() {
        assertThat(mvc.get().uri("/api/v1/sources?synthetic=mozda")).hasStatus(400);
    }
}
