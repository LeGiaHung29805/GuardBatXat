package com.example.GuardBatXat.service;

import com.example.GuardBatXat.dto.request.rescue.LocationCheckRequest;
import com.example.GuardBatXat.dto.response.rescue.LocationCheckResponse;
import com.example.GuardBatXat.dto.response.rescue.NeighborhoodSafetyResponse;

public interface SafetyCheckService {
    LocationCheckResponse evaluateLocationSafety(LocationCheckRequest request);
    NeighborhoodSafetyResponse evaluateNeighborhoodSafety(LocationCheckRequest request);
}