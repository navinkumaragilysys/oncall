package com.oncall.notification.controller;

import com.oncall.notification.dto.request.NotificationPreferenceRequest;
import com.oncall.notification.dto.response.NotificationPreferenceResponse;
import com.oncall.notification.service.NotificationPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notification-preferences")
@RequiredArgsConstructor
public class NotificationPreferenceController {

    private final NotificationPreferenceService prefService;

    @GetMapping
    public ResponseEntity<List<NotificationPreferenceResponse>> list(
            @RequestParam(required = false) UUID memberId) {
        List<NotificationPreferenceResponse> result = memberId != null
                ? prefService.listByMember(memberId)
                : prefService.list();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationPreferenceResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(prefService.getById(id));
    }

    @PostMapping
    public ResponseEntity<NotificationPreferenceResponse> upsert(
            @Valid @RequestBody NotificationPreferenceRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(prefService.upsert(req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        prefService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
