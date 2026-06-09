package com.oncall.identity.repository;

import com.oncall.domain.entity.NotificationPreference;
import com.oncall.domain.enums.NotificationChannel;
import com.oncall.domain.enums.NotificationEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, UUID> {

    List<NotificationPreference> findByMemberId(UUID memberId);

    Optional<NotificationPreference> findByMemberIdAndChannelAndEventType(
            UUID memberId, NotificationChannel channel, NotificationEventType eventType);

    void deleteByMemberId(UUID memberId);
}
