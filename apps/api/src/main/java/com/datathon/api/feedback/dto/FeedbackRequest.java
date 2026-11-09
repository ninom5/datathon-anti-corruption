package com.datathon.api.feedback.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record FeedbackRequest(
        @NotNull(message = "obavezno polje")
        @Pattern(regexp = "indicator|contract", message = "dopuštene vrijednosti su indicator i contract")
        String targetType,
        @NotNull(message = "obavezno polje") @Positive(message = "neispravna vrijednost") Long targetId,
        @NotNull(message = "obavezno polje")
        @Pattern(regexp = "greska_u_podacima|kontekst|pravo_na_odgovor|uklanjanje_osobnih_podataka",
                message = "dopuštene vrijednosti su greska_u_podacima, kontekst, pravo_na_odgovor i uklanjanje_osobnih_podataka")
        String kind,
        @Pattern(regexp = "gradanin|novinar|narucitelj|dobavljac|ostalo",
                message = "dopuštene vrijednosti su gradanin, novinar, narucitelj, dobavljac i ostalo")
        String role,
        @NotBlank(message = "obavezno polje")
        @Size(min = 10, max = 4000, message = "tekst mora imati od 10 do 4.000 znakova")
        String text,
        @Email(message = "neispravna e-mail adresa") @Size(max = 254, message = "predugačka e-mail adresa")
        String contactEmail) {
}
