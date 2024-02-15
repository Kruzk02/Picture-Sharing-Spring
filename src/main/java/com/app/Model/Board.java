package com.app.Model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Board {

    private Long id;
    private User user;
    private String name;

}
