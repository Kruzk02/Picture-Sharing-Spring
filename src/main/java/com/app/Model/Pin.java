package com.app.Model;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Pin {

    private Long id;
    private User user;
    private List<Board> boards;
    private String fileName;
    private String image_url;
    private String description;
}
