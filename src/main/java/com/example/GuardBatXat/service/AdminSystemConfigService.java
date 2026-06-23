package com.example.GuardBatXat.service;
import com.example.GuardBatXat.entity.ModelRegistry;
import com.example.GuardBatXat.entity.AhpWeight;

import com.example.GuardBatXat.dto.request.admin.AhpWeightRequest;
import com.example.GuardBatXat.dto.response.admin.AhpWeightResponse;
import com.example.GuardBatXat.dto.response.admin.ModelRegistryResponse;

import java.util.List;

public interface AdminSystemConfigService {
    List<ModelRegistryResponse> getAllModels();
    ModelRegistryResponse activateModel(Integer modelId);
    AhpWeightResponse updateAhpWeights(String strategyName, AhpWeightRequest request);
}