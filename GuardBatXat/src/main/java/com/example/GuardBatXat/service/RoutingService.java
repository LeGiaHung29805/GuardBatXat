package com.example.GuardBatXat.service;

import com.example.GuardBatXat.dto.request.FindShelterRequest;
import com.example.GuardBatXat.dto.request.RoutingRequest;
import com.example.GuardBatXat.dto.response.RoutingResponse;

public interface RoutingService {
    Object getSafeRouteFromAI(RoutingRequest request);
    Object findSafeShelterFromAI(FindShelterRequest request);
    RoutingResponse findOptimalRoute(String strategyName, RoutingRequest request);
}