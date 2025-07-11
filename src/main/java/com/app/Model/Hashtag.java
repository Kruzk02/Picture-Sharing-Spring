package com.app.Model;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Hashtag implements Serializable {
  private Long id;
  private String tag;
  private LocalDateTime createdAt;
}
