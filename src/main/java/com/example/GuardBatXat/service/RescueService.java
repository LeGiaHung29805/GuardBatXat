package com.example.GuardBatXat.service;

import com.example.GuardBatXat.entity.SosEntity;
//import com.example.GuardBatXat.entity.SosFieldUpdate;

import java.util.List;
import java.util.Map;

public interface RescueService {
    List<SosEntity> getAllSosRequests();
    SosEntity acceptSosRequest(Integer id);
    SosEntity completeSosRequest(Integer id);
    List<Map<String, Object>> getSosFieldUpdates(Integer sosId);
    Map<String, Object> sendSosFieldUpdate(Integer sosId, Map<String, Object> data);
}
