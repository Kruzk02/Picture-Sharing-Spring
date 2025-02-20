package com.app.Model;

import lombok.*;

import java.io.Serializable;
import java.sql.Timestamp;

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
    private Timestamp createdAt;

}
