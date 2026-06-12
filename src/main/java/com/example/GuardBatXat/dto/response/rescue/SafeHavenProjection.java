package com.example.GuardBatXat.dto.response.rescue;
import com.example.GuardBatXat.entity.SafeHaven;

public interface SafeHavenProjection {
    Integer getId();
    String getName();
    String getHavenType(); // VD: "Trường học", "UBND"
    Double getLongitude();
    Double getLatitude();
    Integer getMaxCapacity();
    Integer getCurrentOccupancy();
    Double getDistance(); // Khoảng cách thực tế (mét)
}