package com.app.DTO.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class TokenRequest {
    private String username;
    private boolean isRemember;
    private Map<String, Object> claims;
}
