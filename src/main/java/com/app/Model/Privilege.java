package com.app.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Privilege {
  private Long id;
  private String name;

  public Privilege(String name) {
    this.name = name;
  }
}
