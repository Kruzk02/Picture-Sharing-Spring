package com.app.Model;

import java.io.Serializable;
import java.sql.Date;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VerificationToken implements Serializable {

  private Long id;
  private String token;
  private Long userId;
  private Date expireDate;
}
