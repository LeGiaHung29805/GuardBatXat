package com.example.GuardBatXat.service.impl;
import com.example.GuardBatXat.entity.Notification;

import com.example.GuardBatXat.dto.request.admin.AhpWeightRequest;
import com.example.GuardBatXat.dto.response.admin.AhpWeightResponse;
import com.example.GuardBatXat.dto.response.admin.ModelRegistryResponse;
import com.example.GuardBatXat.entity.AhpWeight;
import com.example.GuardBatXat.entity.ModelRegistry;
import com.example.GuardBatXat.repository.AhpWeightRepository;
import com.example.GuardBatXat.repository.ModelRegistryRepository;
import com.example.GuardBatXat.service.AdminSystemConfigService;
import com.example.GuardBatXat.websocket.NotificationSender;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminSystemConfigServiceImpl implements AdminSystemConfigService {

    private final ModelRegistryRepository modelRegistryRepository;
    private final AhpWeightRepository ahpWeightRepository;
    private final NotificationSender notificationSender;

    private ModelRegistryResponse mapToModelResponse(ModelRegistry entity) {
        return ModelRegistryResponse.builder()
                .id(entity.getId())
                .modelName(entity.getModelName())
                .algorithm(entity.getAlgorithm())
                .modelTarget(entity.getModelTarget())
                .isActive(entity.getIsActive())
                .build();
    }

    private AhpWeightResponse mapToWeightResponse(AhpWeight entity) {
        return AhpWeightResponse.builder()
                .strategyName(entity.getStrategyName())
                .wDistance(entity.getWDistance())
                .wFlood(entity.getWFlood())
                .wLandslide(entity.getWLandslide())
                .wCapacity(entity.getWCapacity())
                .wBridge(entity.getWBridge())
                .wReport(entity.getWReport())
                .build();
    }

    @Override
    @Cacheable(value = "systemModels", key = "'allModels'")
    public List<ModelRegistryResponse> getAllModels() {
        return modelRegistryRepository.findAll().stream()
                .map(this::mapToModelResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "systemModels", allEntries = true)
    public ModelRegistryResponse activateModel(Integer modelId) {
        ModelRegistry targetModel = modelRegistryRepository.findById(modelId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mô hình AI!"));

        // Bước 1: Tắt tất cả các model đang chạy cùng loại
        modelRegistryRepository.deactivateAllModelsByTarget(targetModel.getModelTarget());

        // Bước 2: Bật model được Admin chọn
        targetModel.setIsActive(true);
        ModelRegistry savedModel = modelRegistryRepository.save(targetModel);

        try {
            notificationSender.sendSystemNotification("/topic/system-config-updates", "AI Model Changed: " + savedModel.getModelName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mapToModelResponse(savedModel);
    }

    @Override
    @Transactional
    public AhpWeightResponse updateAhpWeights(String strategyName, AhpWeightRequest request) {
        double sum = request.getWDistance() + request.getWFlood()
                + request.getWLandslide() + request.getWCapacity()
                + request.getWBridge() + request.getWReport();

        if (Math.abs(sum - 1.0) > 0.001) {
            throw new RuntimeException("Tổng 6 trọng số AHP phải chính xác bằng 1.0. Hiện tại đang là: " + sum);
        }

        AhpWeight weight = ahpWeightRepository.findById(strategyName)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chiến lược: " + strategyName));

        weight.setWDistance(request.getWDistance());
        weight.setWFlood(request.getWFlood());
        weight.setWLandslide(request.getWLandslide());
        weight.setWCapacity(request.getWCapacity());
        weight.setWBridge(request.getWBridge());
        weight.setWReport(request.getWReport());

        AhpWeight savedWeight = ahpWeightRepository.save(weight);

        try {
            notificationSender.sendSystemNotification("/topic/system-config-updates", "AHP Weights Changed for: " + strategyName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mapToWeightResponse(savedWeight);
    }
}