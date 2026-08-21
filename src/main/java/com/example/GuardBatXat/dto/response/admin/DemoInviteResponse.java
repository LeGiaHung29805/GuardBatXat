package com.example.GuardBatXat.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemoInviteResponse {
    private String identifier;
    private String token;
    private Instant expiresAt;
}
