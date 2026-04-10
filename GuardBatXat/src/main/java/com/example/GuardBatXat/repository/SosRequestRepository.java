package com.example.GuardBatXat.repository;

import com.example.GuardBatXat.entity.SosEntity; // Dùng Entity duy nhất này
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SosRequestRepository extends JpaRepository<SosEntity, Integer> {

    // Logic 1: Lấy danh sách (Dùng cho Admin)
    List<SosEntity> findByStatus(String status);

    // Logic 2: Lưu dữ liệu không gian (Dùng khi người dân gửi SOS)
    @Modifying
    @Query(value = """
        INSERT INTO batxat_sos_requests (sender_phone, message, gps_lat, gps_lng, geom, status)
        VALUES (
            :phone, :message, :lat, :lng, 
            ST_SetSRID(ST_MakePoint(:lng, :lat), 4326),
            'OPEN'
        )
    """, nativeQuery = true)
    void insertSosRequestNative(
            @Param("phone") String phone,
            @Param("message") String message,
            @Param("lat") Double lat,
            @Param("lng") Double lng
    );
}