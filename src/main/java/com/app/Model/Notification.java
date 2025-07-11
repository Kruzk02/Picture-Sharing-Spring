package com.app.Model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Notification {

  private Long id;
  private Long userId;
  private String message;
  private boolean isRead;
  private LocalDateTime createdAt;
}
