package com.example.GuardBatXat.service;

import com.example.GuardBatXat.dto.request.rescue.FindShelterRequest;
import com.example.GuardBatXat.dto.request.rescue.RoutingRequest;
import com.example.GuardBatXat.dto.response.rescue.RoutingCompareResponse;
import com.example.GuardBatXat.dto.response.rescue.RoutingResponse;

public interface RoutingService {
    Object getSafeRouteFromAI(RoutingRequest request);
    Object findSafeShelterFromAI(FindShelterRequest request);
    RoutingResponse findOptimalRoute(String strategyName, RoutingRequest request);
    RoutingCompareResponse findAdminCompareRoute(RoutingRequest request);
}