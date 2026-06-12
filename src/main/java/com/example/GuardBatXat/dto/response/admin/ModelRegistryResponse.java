package com.example.GuardBatXat.dto.response.admin;
import com.example.GuardBatXat.entity.ModelRegistry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelRegistryResponse {
    private Integer id;
    private String modelName;
    private String algorithm;
    private String modelTarget;
    private Boolean isActive;
}
