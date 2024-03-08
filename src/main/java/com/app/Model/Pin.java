package com.app.Model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Pin {

    private Long id;
    private User user;
    private Board board;
    private String fileName;
    private String image_url;
    private String description;
}
