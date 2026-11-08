package com.datathon.api.common;

/** The one error shape of the API; the frontend shows {@code message}, so it is in Croatian. */
public record ErrorResponse(int statusCode, String message, String error) {
}
