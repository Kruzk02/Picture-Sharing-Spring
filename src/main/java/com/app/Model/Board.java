package com.app.Model;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Board implements Serializable {

    private Long id;
    private User user;
    private String name;
    private List<Pin> pins;

}
