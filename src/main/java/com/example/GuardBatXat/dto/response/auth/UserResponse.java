package com.example.GuardBatXat.dto.response.auth;
import com.example.GuardBatXat.entity.User;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    private Integer userId;
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String roleName;
    private String assignedStation;
    private Boolean isActive;
    private LocalDateTime createdAt;
}