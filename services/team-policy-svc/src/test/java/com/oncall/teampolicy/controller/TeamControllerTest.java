package com.oncall.teampolicy.controller;

import com.oncall.domain.enums.Region;
import com.oncall.teampolicy.dto.request.TeamUpsertRequest;
import com.oncall.teampolicy.dto.response.TeamResponse;
import com.oncall.teampolicy.service.TeamService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class TeamControllerTest {

        private final TeamService teamService = Mockito.mock(TeamService.class);

        private final TeamController teamController = new TeamController(teamService);

    @Test
    void shouldCreateTeam() throws Exception {
        UUID id = UUID.randomUUID();
        TeamUpsertRequest request = new TeamUpsertRequest(
                "Platform Team",
                Region.US_EST,
                null,
                2,
                true
        );

        TeamResponse response = new TeamResponse(
                id,
                "Platform Team",
                Region.US_EST,
                null,
                2,
                true,
                Instant.now(),
                Instant.now()
        );

        Mockito.when(teamService.create(Mockito.any())).thenReturn(response);

        ResponseEntity<TeamResponse> result = teamController.create(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(id, result.getBody().id());
        assertEquals("Platform Team", result.getBody().name());
    }
}
