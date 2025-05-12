package com.app.Model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.Instant;

public record VerificationEmailEvent(
        @NotNull @Email String userEmail,
        @NotNull String verificationToken,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC") @NotNull Instant createdAt
) implements Serializable { }