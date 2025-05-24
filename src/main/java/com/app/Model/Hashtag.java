package com.app.Model;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Hashtag {
    private Long id;
    private String tag;
    private LocalDateTime createdAt;
}
