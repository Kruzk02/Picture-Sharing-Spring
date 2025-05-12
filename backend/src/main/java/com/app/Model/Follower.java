package com.app.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Follower implements Serializable {

    private Long followerId;
    private Long followingId;
    private LocalDateTime followingDate;

}
