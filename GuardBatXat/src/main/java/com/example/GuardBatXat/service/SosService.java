package com.example.GuardBatXat.service;

import com.example.GuardBatXat.dto.request.SosRequest;
import com.example.GuardBatXat.dto.request.LiveLocationRequest;
import com.example.GuardBatXat.dto.request.ChatRequest;

public interface SosService {
    void processSosRequest(SosRequest requestDto);
    void updateLiveLocation(LiveLocationRequest request);
    void sendEmergencyChat(ChatRequest request);
}