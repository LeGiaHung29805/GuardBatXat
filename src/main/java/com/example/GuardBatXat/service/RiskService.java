package com.example.GuardBatXat.service;
import com.example.GuardBatXat.entity.SafeHaven;

import com.example.GuardBatXat.dto.request.rescue.LocationCheckRequest;
import com.example.GuardBatXat.dto.response.commander.EvacuationResponse;
import com.example.GuardBatXat.dto.response.rescue.LocationCheckResponse;

public interface RiskService {
    LocationCheckResponse checkLocationSafety(LocationCheckRequest request);
    EvacuationResponse findNearestSafeHavens(LocationCheckRequest request);
}