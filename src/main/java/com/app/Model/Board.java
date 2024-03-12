package com.app.Model;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Board {

    private Long id;
    private User user;
    private String name;
    private List<Pin> pins;

}
