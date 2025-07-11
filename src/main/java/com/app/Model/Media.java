package com.app.Model;

import java.io.Serializable;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Media implements Serializable {

  private Long id;
  private String url;
  private MediaType mediaType;
  private Timestamp created_at;
}
