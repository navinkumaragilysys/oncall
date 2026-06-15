package com.oncall.notification.controller;

import com.oncall.notification.dto.request.NotificationLogRequest;
import com.oncall.notification.dto.response.NotificationLogResponse;
import com.oncall.notification.service.NotificationLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationLogController {

    private final NotificationLogService logService;

    @GetMapping
    public ResponseEntity<List<NotificationLogResponse>> list(
            @RequestParam(required = false) UUID recipientId) {
        List<NotificationLogResponse> result = recipientId != null
                ? logService.listByRecipient(recipientId)
                : logService.list();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationLogResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(logService.getById(id));
    }

    @PostMapping
    public ResponseEntity<NotificationLogResponse> record(
            @Valid @RequestBody NotificationLogRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(logService.record(req));
    }
}
