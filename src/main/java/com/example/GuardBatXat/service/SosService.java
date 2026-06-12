package com.example.GuardBatXat.service;

import com.example.GuardBatXat.dto.request.SosRequest;
import com.example.GuardBatXat.dto.request.LiveLocationRequest;
import com.example.GuardBatXat.dto.request.ChatRequest;

public interface SosService {
    void processSosRequest(SosRequest requestDto);
    void updateLiveLocation(LiveLocationRequest request);
    void sendEmergencyChat(ChatRequest request);
    java.util.List<com.example.GuardBatXat.dto.response.SosResponse> getAllSosRequests();
    void acceptSosRequest(Integer id, String identifier);
    void completeSosRequest(Integer id, String identifier);
    java.util.List<com.example.GuardBatXat.dto.response.SosUpdateLogResponse> getSosUpdates(Integer id);
    void addSosUpdate(Integer id, com.example.GuardBatXat.dto.request.SosUpdateLogRequest request, String identifier);
}