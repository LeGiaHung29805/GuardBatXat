package com.example.GuardBatXat.service;

import com.example.GuardBatXat.dto.response.commander.HeatmapProjection;
import com.example.GuardBatXat.repository.HeatmapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MapBroadcastService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private HeatmapRepository heatmapRepository;

    // Giả sử có một Scheduler hoặc trigger gọi hàm này khi Python chạy xong
    public void broadcastNewLandslideHeatmap() {

        List<HeatmapProjection> newData = heatmapRepository.getLandslideHeatmap();

        messagingTemplate.convertAndSend("/topic/heatmap-updates", newData);

    }

    public void broadcastFloodHeatmap(Double waterLevel) {
        List<HeatmapProjection> floodData = heatmapRepository.getFloodHeatmap(waterLevel);
        messagingTemplate.convertAndSend("/topic/flood-updates", floodData);
    }
}