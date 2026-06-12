package com.example.GuardBatXat.service;
import com.example.GuardBatXat.entity.SosUpdateLog;
import com.example.GuardBatXat.dto.response.rescue.SosUpdateLogResponse;
import com.example.GuardBatXat.dto.request.rescue.SosUpdateLogRequest;
import com.example.GuardBatXat.dto.request.rescue.LiveLocationRequest;
import com.example.GuardBatXat.dto.request.rescue.ChatRequest;

import com.example.GuardBatXat.dto.request.rescue.SosRequest;
import com.example.GuardBatXat.dto.response.rescue.SosResponse;

import java.util.List;

public interface SosService {
    void processSosRequest(SosRequest requestDto);
    void updateLiveLocation(com.example.GuardBatXat.dto.request.rescue.LiveLocationRequest request);
    void sendEmergencyChat(com.example.GuardBatXat.dto.request.rescue.ChatRequest request);
    List<SosResponse> getAllSosRequests();
    void acceptSosRequest(Integer id, String identifier);
    void completeSosRequest(Integer id, String identifier);
    
    java.util.List<com.example.GuardBatXat.dto.response.rescue.SosUpdateLogResponse> getSosUpdates(Integer sosId);
    void addSosUpdate(Integer sosId, com.example.GuardBatXat.dto.request.rescue.SosUpdateLogRequest request, String identifier);
}