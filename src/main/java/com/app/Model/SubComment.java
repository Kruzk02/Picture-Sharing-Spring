package com.app.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SubComment implements Serializable {

    private Long id;
    private String content;
    private Comment comment;
    private User user;
    private Timestamp timestamp;
}
