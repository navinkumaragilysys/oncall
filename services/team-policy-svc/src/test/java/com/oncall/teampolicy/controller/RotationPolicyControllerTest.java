package com.oncall.teampolicy.controller;

import com.oncall.domain.enums.AllocationStrategy;
import com.oncall.teampolicy.dto.request.RotationPolicyUpsertRequest;
import com.oncall.teampolicy.dto.response.RotationPolicyResponse;
import com.oncall.teampolicy.service.RotationPolicyService;
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
class RotationPolicyControllerTest {

        private final RotationPolicyService rotationPolicyService = Mockito.mock(RotationPolicyService.class);

        private final RotationPolicyController rotationPolicyController = new RotationPolicyController(rotationPolicyService);

    @Test
    void shouldCreatePolicy() throws Exception {
        UUID id = UUID.randomUUID();
        RotationPolicyUpsertRequest request = new RotationPolicyUpsertRequest(
                "Default Policy",
                AllocationStrategy.ROUND_ROBIN,
                7,
                120,
                false,
                true,
                0.5,
                false,
                3,
                false
        );

        RotationPolicyResponse response = new RotationPolicyResponse(
                id,
                "Default Policy",
                AllocationStrategy.ROUND_ROBIN,
                7,
                120,
                false,
                true,
                0.5,
                false,
                3,
                false,
                Instant.now(),
                Instant.now()
        );

        Mockito.when(rotationPolicyService.create(Mockito.any())).thenReturn(response);

        ResponseEntity<RotationPolicyResponse> result = rotationPolicyController.create(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(id, result.getBody().id());
        assertEquals("Default Policy", result.getBody().name());
    }
}
