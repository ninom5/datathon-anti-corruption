package com.datathon.api.feedback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import com.datathon.api.common.NotFoundException;
import com.datathon.api.common.TooManyRequestsException;
import com.datathon.api.feedback.dto.FeedbackResponse;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(FeedbackController.class)
class FeedbackControllerTest {

    private static final String VALID_BODY = """
            {"targetType": "indicator", "targetId": 7, "kind": "kontekst", "role": "narucitelj",
             "text": "Izmjena je predviđena ugovorom, članak 5.", "contactEmail": "procelnik@primjerovo.invalid"}
            """;

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private FeedbackService feedbackService;

    @Test
    void createdFeedbackIsReturnedWithoutTheEmail() {
        given(feedbackService.submit(any(), anyString())).willReturn(
                new FeedbackResponse(41L, "indicator", 7L, "kontekst", "novo", Instant.parse("2026-11-03T20:00:00Z")));

        assertThat(post(VALID_BODY))
                .hasStatus(201)
                .hasHeader("Location", "/api/v1/feedback/41")
                .bodyJson()
                .satisfies(json -> {
                    assertThat(json).extractingPath("$.id").isEqualTo(41);
                    assertThat(json).extractingPath("$.moderation").isEqualTo("novo");
                    assertThat(json).extractingPath("$.createdAt").isEqualTo("2026-11-03T20:00:00Z");
                });
        assertThat(post(VALID_BODY)).bodyText().doesNotContain("procelnik");
    }

    @Test
    void invalidBodiesAreRejectedWithCroatianMessage() {
        assertThat(post(VALID_BODY.replace("\"kontekst\"", "\"korisno\"")))
                .hasStatus(400)
                .bodyJson().extractingPath("$.message").asString().contains("dopuštene vrijednosti");
        assertThat(post(VALID_BODY.replace("Izmjena je predviđena ugovorom, članak 5.", "kratko")))
                .hasStatus(400)
                .bodyJson().extractingPath("$.message").asString().contains("od 10 do 4.000 znakova");
        assertThat(post(VALID_BODY.replace("procelnik@primjerovo.invalid", "nije-adresa"))).hasStatus(400);
        assertThat(post(VALID_BODY.replace("\"indicator\"", "\"organization\""))).hasStatus(400);
    }

    @Test
    void unknownTargetIs404AndTooManyRequestsIs429() {
        given(feedbackService.submit(any(), anyString()))
                .willThrow(new NotFoundException("Zapis na koji se ispravak odnosi ne postoji."))
                .willThrow(new TooManyRequestsException("feedback limit reached"));

        assertThat(post(VALID_BODY)).hasStatus(404);
        assertThat(post(VALID_BODY))
                .hasStatus(429)
                .bodyJson().extractingPath("$.message").asString().contains("Previše zahtjeva");
    }

    private org.springframework.test.web.servlet.assertj.MvcTestResult post(String body) {
        return mvc.post().uri("/api/v1/feedback").contentType(MediaType.APPLICATION_JSON).content(body).exchange();
    }
}
