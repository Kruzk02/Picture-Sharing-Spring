package com.app.Model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class User {

    private Long id;
    private String username;
    private String email;
    private String password;
    private List<Role> roles;

    public List<Role> getRoles() {
        if(roles == null){
            roles = new ArrayList<>();
        }
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }
}