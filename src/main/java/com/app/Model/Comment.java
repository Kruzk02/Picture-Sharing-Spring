package com.app.Model;

import lombok.*;

import java.io.Serializable;
import java.sql.Timestamp;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Comment implements Serializable {

    private Long id;
    private String content;
    private long pinId;
    private long userId;
    private long mediaId;
    private Timestamp created_at;
}
