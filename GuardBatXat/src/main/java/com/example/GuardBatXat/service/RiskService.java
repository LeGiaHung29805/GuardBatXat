package com.example.GuardBatXat.service;

import com.example.GuardBatXat.dto.request.LocationCheckRequest;
import com.example.GuardBatXat.dto.response.LocationCheckResponse;

public interface RiskService {
    LocationCheckResponse checkLocationSafety(LocationCheckRequest request);
}