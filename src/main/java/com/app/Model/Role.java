package com.app.Model;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Role {

    private Long id;
    private String name;
    private User user;
    private List<Privilege> privileges;
}
