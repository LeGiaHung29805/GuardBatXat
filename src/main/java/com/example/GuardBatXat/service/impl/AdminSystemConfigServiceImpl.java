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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminSystemConfigServiceImpl implements AdminSystemConfigService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal WEIGHT_SUM_TOLERANCE = new BigDecimal("0.001");

    private final ModelRegistryRepository modelRegistryRepository;
    private final AhpWeightRepository ahpWeightRepository;
    private final NotificationSender notificationSender;
    private final RestTemplate restTemplate;

    @Value("${batxat.ai.service.base-url:http://localhost:5000}")
    private String aiServiceBaseUrl;

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

        String target = targetModel.getModelTarget();

        validateModelWithAiService(targetModel);

        // Bước 1: Tắt tất cả các model đang chạy cùng loại
        modelRegistryRepository.deactivateAllModelsByTarget(target);

        // Bước 2: Tải lại thực thể mới từ DB và bật model được Admin chọn
        ModelRegistry freshModel = modelRegistryRepository.findById(modelId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mô hình AI!"));
        freshModel.setIsActive(true);
        ModelRegistry savedModel = modelRegistryRepository.save(freshModel);

        try {
            notificationSender.sendSystemNotification("/topic/system-config-updates", "AI Model Changed: " + savedModel.getModelName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mapToModelResponse(savedModel);
    }

    private void validateModelWithAiService(ModelRegistry model) {
        Map<String, String> request = new HashMap<>();
        request.put("modelTarget", model.getModelTarget());
        request.put("modelPath", model.getModelPath());
        request.put("scalerPath", model.getScalerPath());

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    aiServiceBaseUrl + "/api/v1/ai/models/validate",
                    request,
                    Map.class
            );
            if (response == null || !Boolean.TRUE.equals(response.get("valid"))) {
                throw new IllegalStateException("AI service không xác nhận được model");
            }
        } catch (Exception exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Không thể kích hoạt model vì file/model scaler chưa hợp lệ",
                    exception
            );
        }
    }

        @Override
    public AhpWeightResponse getAhpWeights(String strategyName) {
        AhpWeight weight = ahpWeightRepository.findById(strategyName)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chiến lược: " + strategyName));
        return mapToWeightResponse(weight);
    }

    @Override
    @Transactional
    public AhpWeightResponse updateAhpWeights(String strategyName, AhpWeightRequest request) {
        BigDecimal sum = request.getWDistance().add(request.getWFlood())
                .add(request.getWLandslide()).add(request.getWCapacity())
                .add(request.getWBridge()).add(request.getWReport());

        if (sum.subtract(ONE).abs().compareTo(WEIGHT_SUM_TOLERANCE) > 0) {
            throw new IllegalArgumentException("Tổng 6 trọng số AHP phải bằng 1.0. Hiện tại là: " + sum);
        }

        if (isOutsideUnitRange(request.getWDistance())
                || isOutsideUnitRange(request.getWFlood())
                || isOutsideUnitRange(request.getWLandslide())
                || isOutsideUnitRange(request.getWCapacity())
                || isOutsideUnitRange(request.getWBridge())
                || isOutsideUnitRange(request.getWReport())) {
            throw new IllegalArgumentException("Mỗi trọng số AHP phải nằm trong khoảng từ 0 đến 1");
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

    private boolean isOutsideUnitRange(BigDecimal value) {
        return value.compareTo(ZERO) < 0 || value.compareTo(ONE) > 0;
    }
}
