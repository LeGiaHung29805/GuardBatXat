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

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminSystemConfigServiceImpl implements AdminSystemConfigService {

    private final ModelRegistryRepository modelRegistryRepository;
    private final AhpWeightRepository ahpWeightRepository;

    @Override
    public List<ModelRegistry> getAllModels() {
        return modelRegistryRepository.findAll();
    }

    @Override
    @Transactional
    public ModelRegistry activateModel(Integer modelId) {
        ModelRegistry targetModel = modelRegistryRepository.findById(modelId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mô hình AI!"));

        // Bước 1: Tắt tất cả các model đang chạy cùng loại (Ví dụ: tắt model ngập lụt cũ)
        modelRegistryRepository.deactivateAllModelsByTarget(targetModel.getModelTarget());

        // Bước 2: Bật model được Admin chọn
        targetModel.setIsActive(true);
        return modelRegistryRepository.save(targetModel);
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

        return ahpWeightRepository.save(weight);
    }
}