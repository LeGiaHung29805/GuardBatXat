package com.example.GuardBatXat.repository;

import com.example.GuardBatXat.entity.WeatherStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeatherStationRepository extends JpaRepository<WeatherStation, String> {
}
