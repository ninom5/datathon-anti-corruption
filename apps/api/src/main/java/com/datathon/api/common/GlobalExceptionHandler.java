package com.datathon.api.common;

// Origin: copied from repo Desktop/datathon (branch sime/ingest-other-sources), common/GlobalExceptionHandler.java.

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Exceptions carry English messages for logs, but the text the user sees is Croatian.
 * Rejected values are never logged or returned: a bad OIB or a search text can be personal data.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String INVALID_VALUE = "neispravna vrijednost";
    private static final String INVALID_REQUEST = "Neispravan zahtjev. Provjerite unesene podatke.";

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ErrorResponse> handleNotFound(NotFoundException e) {
        return respond(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ErrorResponse> handleUnknownRoute(NoResourceFoundException e) {
        return respond(HttpStatus.NOT_FOUND, "Tražena adresa ne postoji.");
    }

    @ExceptionHandler(InvalidRequestException.class)
    ResponseEntity<ErrorResponse> handleInvalidRequest(InvalidRequestException e) {
        return badRequest(e.getMessage());
    }

    /** Covers {@code @Valid} on a body and on a {@code @ModelAttribute}; the former extends this exception. */
    @ExceptionHandler(BindException.class)
    ResponseEntity<ErrorResponse> handleBind(BindException e) {
        return badRequest(firstBindingMessage(e.getBindingResult()));
    }

    /** Constraints placed directly on a controller parameter, for example {@code @RequestParam @Min(1)}. */
    @ExceptionHandler(HandlerMethodValidationException.class)
    ResponseEntity<ErrorResponse> handleParameterValidation(HandlerMethodValidationException e) {
        String message = e.getParameterValidationResults().stream()
                .findFirst()
                .map(GlobalExceptionHandler::parameterMessage)
                .orElse(INVALID_REQUEST);
        return badRequest(message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return badRequest(e.getName() + ": " + INVALID_VALUE);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException e) {
        return badRequest(e.getParameterName() + ": obavezan parametar");
    }

    /** The exception text quotes the offending JSON value, so only the type is logged. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException e) {
        log.info("Rejected request: unreadable body ({})", e.getClass().getSimpleName());
        return respond(HttpStatus.BAD_REQUEST, "Neispravno tijelo zahtjeva.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return respond(HttpStatus.METHOD_NOT_ALLOWED, "Metoda nije podržana za ovu adresu.");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        return respond(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Format zahtjeva nije podržan.");
    }

    @ExceptionHandler(ExternalSourceException.class)
    ResponseEntity<ErrorResponse> handleExternalSource(ExternalSourceException e) {
        log.warn("External source {} failed", e.getSource(), e);
        return respond(HttpStatus.BAD_GATEWAY,
                "Vanjski izvor (" + e.getSource() + ") trenutno nije dostupan. Pokušajte kasnije.");
    }

    @ExceptionHandler(TooManyRequestsException.class)
    ResponseEntity<ErrorResponse> handleTooManyRequests(TooManyRequestsException e) {
        log.warn("Rate limit hit: {}", e.getMessage());
        return respond(HttpStatus.TOO_MANY_REQUESTS, "Previše zahtjeva. Pokušajte ponovno za minutu.");
    }

    /** Admin routes are not for end users, so the reason is passed through. */
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ErrorResponse> handleStatus(ResponseStatusException e) {
        HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
        return respond(status, e.getReason() != null ? e.getReason() : status.getReasonPhrase());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        log.error("Unexpected error", e);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "Došlo je do pogreške na poslužitelju.");
    }

    private static String firstBindingMessage(BindingResult result) {
        FieldError fieldError = result.getFieldError();
        if (fieldError != null) {
            // A binding failure (for example text in a date) has a default message that quotes the value.
            String reason = fieldError.isBindingFailure() ? INVALID_VALUE : fieldError.getDefaultMessage();
            return fieldError.getField() + ": " + reason;
        }
        ObjectError globalError = result.getGlobalError();
        return globalError != null && globalError.getDefaultMessage() != null
                ? globalError.getDefaultMessage()
                : INVALID_REQUEST;
    }

    private static String parameterMessage(ParameterValidationResult result) {
        String reason = result.getResolvableErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse(INVALID_VALUE);
        return result.getMethodParameter().getParameterName() + ": " + reason;
    }

    private ResponseEntity<ErrorResponse> badRequest(String message) {
        log.info("Rejected request: {}", message);
        return respond(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseEntity<ErrorResponse> respond(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), message, status.getReasonPhrase()));
    }
}
