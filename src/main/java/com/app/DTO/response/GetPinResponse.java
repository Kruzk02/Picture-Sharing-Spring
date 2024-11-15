package com.app.DTO.response;

public record GetPinResponse(
        Long userId,
        String image_url,
        String description
) { }
