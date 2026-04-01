package com.example.GuardBatXat.service;

import com.example.GuardBatXat.dto.request.RoutingRequest;

public interface RoutingService {
    Object getSafeRouteFromAI(RoutingRequest request);
}