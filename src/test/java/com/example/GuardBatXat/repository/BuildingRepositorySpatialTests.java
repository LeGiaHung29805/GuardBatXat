package com.example.GuardBatXat.repository;

import com.example.GuardBatXat.repository.projection.BuildingLocationView;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class BuildingRepositorySpatialTests {

    @Autowired
    private BuildingRepository buildingRepository;

    @ParameterizedTest
    @CsvSource({
            "103.90202524365233, 22.52539385482902",
            "103.8895, 22.5458",
            "105.8342, 21.0278"
    })
    void nearestLocationReturnsFinitePointWithoutHydratingGeometry(
            double longitude,
            double latitude
    ) {
        BuildingLocationView location = buildingRepository
                .findNearestLocation(longitude, latitude)
                .orElseThrow();

        assertNotNull(location.getBuildingId());
        assertTrue(Double.isFinite(location.getLatitude()));
        assertTrue(Double.isFinite(location.getLongitude()));
    }
}
