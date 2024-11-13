package com.app.Model;

import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements Serializable {

    private Long id;
    private String username;
    private String email;
    private String password;
    private List<Role> roles;
    private String profilePicture;
    private String bio;
    private Gender gender;

}