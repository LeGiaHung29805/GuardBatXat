package com.example.GuardBatXat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.example.GuardBatXat.entity.RoadEdgeId;
import com.example.GuardBatXat.repository.RoadEdgeRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
		"batxat.security.jwt.secret=GuardBatXatTestSecretKeyMustBeAtLeast32BytesLong"
})
class GuardBatXatApplicationTests {

	@Autowired
	private RoadEdgeRepository roadEdgeRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void contextLoads() {
	}

	@Test
	@Transactional
	void adminRoadInsertUsesCanonicalGeometryColumn() {
		List<Long> nodeIds = jdbcTemplate.queryForList(
				"SELECT node_id FROM batxat_road_nodes ORDER BY node_id LIMIT 2",
				Long.class
		);
		Long u = nodeIds.get(0);
		Long v = nodeIds.get(1);
		Integer key = Integer.MAX_VALUE;

		roadEdgeRepository.insertRoadEdgeNative(
				u, v, key, 10.0, 3, 0,
				"LINESTRING(103.8 22.5, 103.8001 22.5001)"
		);

		assertTrue(roadEdgeRepository.existsById(new RoadEdgeId(u, v, key)));
	}

}
