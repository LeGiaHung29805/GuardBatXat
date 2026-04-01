package com.example.GuardBatXat.repository;

import com.example.GuardBatXat.entity.SosRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SosRequestRepository extends JpaRepository<SosRequest, Long> {

    @Modifying
    @Query(value = """
        INSERT INTO batxat_sos_requests (sender_phone, message, gps_lat, gps_lng, geom, building_id)
        VALUES (
            :phone, :message, :lat, :lng, 
            ST_SetSRID(ST_MakePoint(:lng, :lat), 4326),
            (SELECT id FROM batxat_buildings WHERE ST_Contains(geom, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)) LIMIT 1)
        )
    """, nativeQuery = true)
    void insertSosRequestNative(
            @Param("phone") String phone,
            @Param("message") String message,
            @Param("lat") Double lat,
            @Param("lng") Double lng
    );
}