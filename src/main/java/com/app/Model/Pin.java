package com.app.Model;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
public class Pin implements Serializable {

    private Long id;
    private Long userId;
    private String description;
    private Long mediaId;
    private Collection<Hashtag> hashtags;
    private LocalDateTime createdAt;

}
