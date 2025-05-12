package com.app.DTO.response;

import java.time.LocalDateTime;

public record NotificationResponse(Long id, Long userId, String message, Boolean isRead, LocalDateTime createdAt) {
}
