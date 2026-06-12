package com.example.GuardBatXat.service.impl;

import com.example.GuardBatXat.dto.request.AhpWeightRequest;
import com.example.GuardBatXat.entity.AhpWeight;
import com.example.GuardBatXat.entity.ModelRegistry;
import com.example.GuardBatXat.repository.AhpWeightRepository;
import com.example.GuardBatXat.repository.ModelRegistryRepository;
import com.example.GuardBatXat.service.AdminSystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import java.util.List;
import com.example.GuardBatXat.websocket.NotificationSender;

@Service
@RequiredArgsConstructor
public class AdminSystemConfigServiceImpl implements AdminSystemConfigService {

    private final ModelRegistryRepository modelRegistryRepository;
    private final AhpWeightRepository ahpWeightRepository;
    private final NotificationSender notificationSender;

    @Override
    @Cacheable(value = "systemModels", key = "'allModels'")
    public List<ModelRegistry> getAllModels() {
        return modelRegistryRepository.findAll();
    }

    @Override
    @Transactional
    @CacheEvict(value = "systemModels", allEntries = true)
    public ModelRegistry activateModel(Integer modelId) {
        ModelRegistry targetModel = modelRegistryRepository.findById(modelId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mô hình AI!"));

        // Bước 1: Tắt tất cả các model đang chạy cùng loại (Ví dụ: tắt model ngập lụt cũ)
        modelRegistryRepository.deactivateAllModelsByTarget(targetModel.getModelTarget());

        // Bước 2: Bật model được Admin chọn
        targetModel.setIsActive(true);
        ModelRegistry savedModel = modelRegistryRepository.save(targetModel);

        // Phát thông báo cho Client biết AI Model đã đổi
        try {
            notificationSender.sendSystemNotification("/topic/system-config-updates", "AI Model Changed: " + savedModel.getModelName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return savedModel;
    }

    @Override
    @Transactional
    public AhpWeight updateAhpWeights(String strategyName, AhpWeightRequest request) {
        // Bổ sung chốt chặn: Tổng 6 trọng số AHP BẮT BUỘC phải bằng 1.0
        double sum = request.getWDistance() + request.getWFlood()
                + request.getWLandslide() + request.getWCapacity()
                + request.getWBridge() + request.getWReport();

        if (Math.abs(sum - 1.0) > 0.001) {
            throw new RuntimeException("Tổng 6 trọng số AHP phải chính xác bằng 1.0. Hiện tại đang là: " + sum);
        }

        AhpWeight weight = ahpWeightRepository.findById(strategyName)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chiến lược: " + strategyName));

        // Ghi đè các trọng số mới để thay đổi thuật toán tìm đường
        weight.setWDistance(request.getWDistance());
        weight.setWFlood(request.getWFlood());
        weight.setWLandslide(request.getWLandslide());
        weight.setWCapacity(request.getWCapacity());
        weight.setWBridge(request.getWBridge());
        weight.setWReport(request.getWReport());

        AhpWeight savedWeight = ahpWeightRepository.save(weight);

        // Phát thông báo cho Client biết trọng số tìm đường đã đổi
        try {
            notificationSender.sendSystemNotification("/topic/system-config-updates", "AHP Weights Changed for: " + strategyName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return savedWeight;
    }
}