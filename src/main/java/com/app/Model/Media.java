package com.app.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Media {

    private Long id;
    private String url;
    private MediaType mediaType;
    private Timestamp created_at;

}
