package com.example.GuardBatXat.service;

import com.example.GuardBatXat.dto.request.rescue.LocationCheckRequest;
import com.example.GuardBatXat.dto.response.rescue.LocationCheckResponse;

public interface SafetyCheckService {
    LocationCheckResponse evaluateLocationSafety(LocationCheckRequest request);
}