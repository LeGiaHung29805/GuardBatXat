package com.example.GuardBatXat.service;

import com.example.GuardBatXat.dto.request.SosRequest;
import com.example.GuardBatXat.dto.response.SosResponse;

import java.util.List;

public interface SosService {
    void processSosRequest(SosRequest requestDto);
    List<SosResponse> getAllSosRequests();
    void acceptSosRequest(Integer id, String identifier);
    void completeSosRequest(Integer id, String identifier);
    
    java.util.List<com.example.GuardBatXat.dto.response.SosUpdateLogResponse> getSosUpdates(Integer sosId);
    void addSosUpdate(Integer sosId, com.example.GuardBatXat.dto.request.SosUpdateLogRequest request, String identifier);
}