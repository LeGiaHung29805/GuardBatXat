package com.example.GuardBatXat.service;

import com.example.GuardBatXat.dto.request.RoutingRequest;
import com.example.GuardBatXat.dto.response.RoutingResponse;

public interface RoutingService {
    RoutingResponse findOptimalRoute(String strategyName, RoutingRequest request);
}