package com.app.Model;

import lombok.*;

import java.util.Collection;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Role {

    private Long id;
    private String name;
    private Collection<Privilege> privileges;

    public Role(String name) {
        this.name = name;
    }
}
