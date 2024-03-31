package com.app.Model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Comment {

    private Long id;
    private String content;
    private Pin pin;
    private User user;

}
