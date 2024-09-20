package com.app.Model;

import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Pin implements Serializable {

    private Long id;
    private Long userId;
    private String fileName;
    private String image_url;
    private String description;
}
