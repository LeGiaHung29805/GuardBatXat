package com.example.GuardBatXat.dto.request;

import lombok.Data;

@Data
public class AlertRequest {
    private String title;
    private String content;
    private String alertLevel; // INFO, WARNING, EMERGENCY
    private Integer targetRoleId;
}