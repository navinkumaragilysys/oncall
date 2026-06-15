package com.oncall.teampolicy.controller;

import com.oncall.teampolicy.dto.request.HolidayCalendarUpsertRequest;
import com.oncall.teampolicy.dto.response.HolidayCalendarResponse;
import com.oncall.teampolicy.service.HolidayCalendarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/holidays")
@RequiredArgsConstructor
public class HolidayCalendarController {

    private final HolidayCalendarService holidayCalendarService;

    @GetMapping
    public ResponseEntity<List<HolidayCalendarResponse>> list() {
        return ResponseEntity.ok(holidayCalendarService.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HolidayCalendarResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(holidayCalendarService.getById(id));
    }

    @PostMapping
    public ResponseEntity<HolidayCalendarResponse> create(@Valid @RequestBody HolidayCalendarUpsertRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(holidayCalendarService.create(req));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<HolidayCalendarResponse> update(@PathVariable UUID id, @Valid @RequestBody HolidayCalendarUpsertRequest req) {
        return ResponseEntity.accepted().body(holidayCalendarService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        holidayCalendarService.delete(id);
        return ResponseEntity.accepted().build();
    }
}
