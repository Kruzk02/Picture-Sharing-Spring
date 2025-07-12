package com.app.Model;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Follower implements Serializable {

  private Long followerId;
  private Long followingId;
  private LocalDateTime followingDate;
}
