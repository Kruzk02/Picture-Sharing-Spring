package com.app.Model;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

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
