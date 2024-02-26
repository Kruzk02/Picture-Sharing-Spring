package com.app.Model;

import lombok.*;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Role {

    private Long id;
    private String name;

    public Role(String name) {
        this.name = name;
    }
}
