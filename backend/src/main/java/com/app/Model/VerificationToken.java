package com.app.Model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.sql.Date;

@Data
@Builder
public class VerificationToken implements Serializable {

    private Long id;
    private String token;
    private Long userId;
    private Date expireDate;

}
