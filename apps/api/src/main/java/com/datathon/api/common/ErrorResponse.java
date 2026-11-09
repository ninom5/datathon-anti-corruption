package com.datathon.api.common;

public record ErrorResponse(int statusCode, String message, String error) {
}
