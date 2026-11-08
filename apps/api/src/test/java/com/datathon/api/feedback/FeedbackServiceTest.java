package com.datathon.api.feedback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.datathon.api.common.InvalidRequestException;
import com.datathon.api.common.NotFoundException;
import com.datathon.api.common.TooManyRequestsException;
import com.datathon.api.contract.ContractService;
import com.datathon.api.feedback.dto.FeedbackRequest;
import com.datathon.api.indicator.IndicatorService;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class FeedbackServiceTest {

    private final FeedbackRepository repository = mock(FeedbackRepository.class);
    private final IndicatorService indicatorService = mock(IndicatorService.class);
    private final ContractService contractService = mock(ContractService.class);
    private final FeedbackService service = new FeedbackService(repository, new FeedbackRateLimiter(), indicatorService,
            contractService);

    @Test
    void savesTrimmedFeedbackForAnActiveIndicator() {
        given(indicatorService.isActive(7L)).willReturn(true);
        given(repository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        service.submit(request("indicator", 7L, "kontekst", "  Izmjena je predviđena ugovorom.  ", " "), "10.0.0.1");

        ArgumentCaptor<Feedback> saved = ArgumentCaptor.forClass(Feedback.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getText()).isEqualTo("Izmjena je predviđena ugovorom.");
        assertThat(saved.getValue().getContactEmail()).isNull();
        assertThat(saved.getValue().getRole()).isEqualTo(FeedbackRole.BUYER);
        assertThat(saved.getValue().getModeration()).isEqualTo(ModerationStatus.NEW);
    }

    @Test
    void hiddenContractGetsTheSame404AsAMissingOne() {
        given(contractService.isPublic(5L)).willReturn(false);

        assertThatThrownBy(() -> service.submit(request("contract", 5L, "greska_u_podacima", "Iznos je krivo upisan.", null),
                "10.0.0.2")).isInstanceOf(NotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void rightOfReplyNeedsAContactAddress() {
        assertThatThrownBy(() -> service.submit(request("indicator", 7L, "pravo_na_odgovor", "Odgovor općine na oznaku.", ""),
                "10.0.0.3")).isInstanceOf(InvalidRequestException.class).hasMessageContaining("contactEmail");
    }

    @Test
    void rateLimiterAllowsTenPerMinutePerClient() {
        MutableClock clock = new MutableClock();
        FeedbackRateLimiter limiter = new FeedbackRateLimiter(clock);
        for (int i = 0; i < FeedbackRateLimiter.LIMIT; i++) {
            limiter.acquire("10.0.0.4");
        }

        assertThatThrownBy(() -> limiter.acquire("10.0.0.4")).isInstanceOf(TooManyRequestsException.class);
        limiter.acquire("10.0.0.5");
        clock.advance(FeedbackRateLimiter.WINDOW.plusSeconds(1));
        limiter.acquire("10.0.0.4");
    }

    private static FeedbackRequest request(String targetType, Long targetId, String kind, String text, String email) {
        return new FeedbackRequest(targetType, targetId, kind, "narucitelj", text, email);
    }

    private static final class MutableClock extends Clock {

        private Instant now = Instant.parse("2026-11-03T20:00:00Z");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }
    }
}
