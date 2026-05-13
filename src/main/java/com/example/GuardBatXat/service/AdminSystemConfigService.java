package com.example.GuardBatXat.service;

import com.example.GuardBatXat.dto.request.AhpWeightRequest;
import com.example.GuardBatXat.entity.AhpWeight;
import com.example.GuardBatXat.entity.ModelRegistry;
import java.util.List;

public interface AdminSystemConfigService {
    List<ModelRegistry> getAllModels();
    ModelRegistry activateModel(Integer modelId);
    AhpWeight updateAhpWeights(String strategyName, AhpWeightRequest request);
}