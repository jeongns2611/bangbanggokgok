package com.ssafy.backend.domain.infrastructure.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ssafy.backend.domain.house.entity.CurrentHouse;
import com.ssafy.backend.domain.house.repository.CurrentHouseRepository;
import com.ssafy.backend.domain.infrastructure.dto.response.InfrastructureMarkerRow;
import com.ssafy.backend.domain.infrastructure.dto.response.InfrastructureSummaryRow;
import com.ssafy.backend.domain.infrastructure.dto.response.NearbyInfrastructureResponse;
import com.ssafy.backend.domain.infrastructure.repository.InfrastructureRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

class InfrastructureServiceTest {

    private final CurrentHouseRepository currentHouseRepository = mock(CurrentHouseRepository.class);
    private final InfrastructureRepository infrastructureRepository = mock(InfrastructureRepository.class);

    private final InfrastructureService service = new InfrastructureService(
            currentHouseRepository,
            infrastructureRepository
    );

    @Test
    void getNearbyInfrastructures_mapsSummaryRowsAndMarkers() {
        CurrentHouse house = new CurrentHouse();
        house.setPosition(new GeometryFactory().createPoint(new Coordinate(127.0, 37.5)));
        when(currentHouseRepository.findById(1L)).thenReturn(Optional.of(house));
        when(infrastructureRepository.findInfrastructureSummary(1L, 800)).thenReturn(List.of(
                InfrastructureSummaryRow.builder().type("CONVENIENCE").count(3).nearestDistanceMeters(120).build(),
                InfrastructureSummaryRow.builder().type("LAUNDRY").count(1).nearestDistanceMeters(250).build(),
                InfrastructureSummaryRow.builder().type("CAFE").count(4).nearestDistanceMeters(80).build(),
                InfrastructureSummaryRow.builder().type("HOSPITAL").count(2).nearestDistanceMeters(400).build(),
                InfrastructureSummaryRow.builder().type("PHARMACY").count(2).nearestDistanceMeters(160).build(),
                InfrastructureSummaryRow.builder().type("BUS").count(5).nearestDistanceMeters(90).build(),
                InfrastructureSummaryRow.builder().type("SUBWAY").count(1).nearestDistanceMeters(300).build()
        ));
        when(infrastructureRepository.findInfrastructureMarkers(1L, 800)).thenReturn(List.of(
                InfrastructureMarkerRow.builder()
                        .id(11L)
                        .type("CAFE")
                        .name("Cafe A")
                        .latitude(37.501)
                        .longitude(127.001)
                        .build()
        ));

        NearbyInfrastructureResponse result = service.getNearbyInfrastructures(1L);

        assertThat(result.getHouseId()).isEqualTo(1L);
        assertThat(result.getRadiusMeters()).isEqualTo(800);
        assertThat(result.getSummaries()).hasSize(7);
        assertThat(result.getSummaries())
                .extracting(NearbyInfrastructureResponse.Summary::getType)
                .contains("CONVENIENCE", "BUS", "SUBWAY");
        assertThat(result.getMarkers()).hasSize(1);
        assertThat(result.getMarkers().get(0).getName()).isEqualTo("Cafe A");
    }
}
