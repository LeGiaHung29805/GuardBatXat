package com.example.GuardBatXat.repository;

import com.example.GuardBatXat.entity.SosEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SosRequestRepository extends JpaRepository<SosEntity, Integer> {

    List<SosEntity> findByStatus(String status);

    @Modifying
    @Query(value = """
        INSERT INTO batxat_sos_requests (
            sender_phone, message, gps_lat, gps_lng, geom, status,
            sender_name, total_people, elderly_count, children_count, sender_id
        )
        VALUES (
            :phone, :message, :lat, :lng, 
            ST_SetSRID(ST_MakePoint(:lng, :lat), 4326),
            'OPEN',
            :senderName, :totalPeople, :elderlyCount, :childrenCount, :senderId
        )
    """, nativeQuery = true)
    void insertSosRequestNative(
            @Param("phone") String phone,
            @Param("message") String message,
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("senderName") String senderName,
            @Param("totalPeople") Integer totalPeople,
            @Param("elderlyCount") Integer elderlyCount,
            @Param("childrenCount") Integer childrenCount,
            @Param("senderId") Integer senderId
    );
}