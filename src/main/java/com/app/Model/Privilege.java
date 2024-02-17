package com.app.Model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Privilege {

    private Long id;
    private String name;

    public Privilege(String name) {
        this.name = name;
    }
}
