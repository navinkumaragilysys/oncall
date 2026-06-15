package com.oncall.teampolicy.service;

import com.oncall.domain.entity.ShiftDefinition;
import com.oncall.teampolicy.dto.request.ShiftDefinitionUpsertRequest;
import com.oncall.teampolicy.dto.response.ShiftDefinitionResponse;
import com.oncall.teampolicy.exception.ResourceNotFoundException;
import com.oncall.teampolicy.outbox.OutboxPublisher;
import com.oncall.teampolicy.repository.ShiftDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShiftDefinitionService {

    private final ShiftDefinitionRepository shiftDefinitionRepository;
    private final OutboxPublisher outboxPublisher;

    @Transactional(readOnly = true)
    public List<ShiftDefinitionResponse> list() {
        return shiftDefinitionRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ShiftDefinitionResponse getById(UUID id) {
        return toResponse(getEntity(id));
    }

    @Transactional
    public ShiftDefinitionResponse create(ShiftDefinitionUpsertRequest req) {
        ShiftDefinition row = new ShiftDefinition();
        apply(row, req);
        ShiftDefinition saved = shiftDefinitionRepository.save(row);
        outboxPublisher.publish("shift_definition", saved.getId(), "shift_definition.created", toResponse(saved));
        return toResponse(saved);
    }

    @Transactional
    public ShiftDefinitionResponse update(UUID id, ShiftDefinitionUpsertRequest req) {
        ShiftDefinition row = getEntity(id);
        apply(row, req);
        ShiftDefinition saved = shiftDefinitionRepository.save(row);
        outboxPublisher.publish("shift_definition", saved.getId(), "shift_definition.updated", toResponse(saved));
        return toResponse(saved);
    }

    @Transactional
    public void delete(UUID id) {
        ShiftDefinition row = getEntity(id);
        shiftDefinitionRepository.delete(row);
        outboxPublisher.publish("shift_definition", id, "shift_definition.deleted", "{}");
    }

    private ShiftDefinition getEntity(UUID id) {
        return shiftDefinitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift definition not found: " + id));
    }

    private void apply(ShiftDefinition row, ShiftDefinitionUpsertRequest req) {
        row.setShiftType(req.shiftType());
        row.setRegion(req.region());
        row.setDstAware(req.dstAware());
        row.setStandardStartDay(req.standardStartDay());
        row.setStandardStartTime(req.standardStartTime());
        row.setStandardEndDay(req.standardEndDay());
        row.setStandardEndTime(req.standardEndTime());
        row.setDstStartDay(req.dstStartDay());
        row.setDstStartTime(req.dstStartTime());
        row.setDstEndDay(req.dstEndDay());
        row.setDstEndTime(req.dstEndTime());
    }

    private ShiftDefinitionResponse toResponse(ShiftDefinition row) {
        return new ShiftDefinitionResponse(
                row.getId(),
                row.getShiftType(),
                row.getRegion(),
                row.isDstAware(),
                row.getStandardStartDay(),
                row.getStandardStartTime(),
                row.getStandardEndDay(),
                row.getStandardEndTime(),
                row.getDstStartDay(),
                row.getDstStartTime(),
                row.getDstEndDay(),
                row.getDstEndTime(),
                row.getCreatedAt(),
                row.getUpdatedAt()
        );
    }
}
