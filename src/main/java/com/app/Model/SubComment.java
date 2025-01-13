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

    private long id;
    private String content;
    private Comment comment;
    private User user;
    private Media media;
    private Timestamp createAt;

}
