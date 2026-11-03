package com.datathon.api.feedback;

import com.datathon.api.feedback.dto.FeedbackRequest;
import com.datathon.api.feedback.dto.FeedbackResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Corrections and the right of reply (F12). Write-only: nothing sent here is public before moderation. */
@RestController
@RequestMapping("/api/v1/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<FeedbackResponse> submit(@Valid @RequestBody FeedbackRequest request,
                                                   HttpServletRequest servletRequest) {
        FeedbackResponse created = feedbackService.submit(request, servletRequest.getRemoteAddr());
        return ResponseEntity.created(URI.create("/api/v1/feedback/" + created.id())).body(created);
    }
}
