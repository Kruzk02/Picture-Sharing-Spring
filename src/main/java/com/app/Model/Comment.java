package com.app.Model;

import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Comment implements Serializable {

    private Long id;
    private String content;
    private Media media;
    private Pin pin;
    private User user;

}
