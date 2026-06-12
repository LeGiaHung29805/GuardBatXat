package com.example.GuardBatXat.dto.response;

public interface CommanderFloodProjection {
    String getGeojson();
    String getLoai_nha();
    Integer getSo_nguoi();
    Double getCao_do();
    Double getMuc_do();
    Double getDien_tich();
    Double getLat();
    Double getLng();
}
