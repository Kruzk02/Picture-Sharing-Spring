package com.app.DTO;

import com.app.Model.Pin;
import com.app.Model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {

    private String content;
    private User user;
    private Pin pin;
}
