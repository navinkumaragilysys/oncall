package com.oncall.identity.mapper;

import com.oncall.domain.entity.Member;
import com.oncall.domain.entity.NotificationPreference;
import com.oncall.domain.enums.MemberStatus;
import com.oncall.domain.enums.NotificationChannel;
import com.oncall.domain.enums.NotificationEventType;
import com.oncall.domain.enums.Region;
import com.oncall.domain.enums.SystemRole;
import com.oncall.identity.dto.response.MemberResponse;
import com.oncall.identity.dto.response.NotificationPreferenceResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-09T12:50:35+0000",
    comments = "version: 1.6.2, compiler: javac, environment: Java 17.0.19 (Ubuntu)"
)
@Component
public class IdentityMapperImpl implements IdentityMapper {

    @Override
    public MemberResponse toMemberResponse(Member member) {
        if ( member == null ) {
            return null;
        }

        UUID managerId = null;
        String managerName = null;
        UUID id = null;
        String fullName = null;
        String displayName = null;
        String email = null;
        Region region = null;
        String timezone = null;
        MemberStatus status = null;
        boolean oncallEligible = false;
        LocalDate joinDate = null;
        String slackHandle = null;
        String phone = null;
        Set<SystemRole> systemRoles = null;
        Instant createdAt = null;
        Instant updatedAt = null;

        managerId = memberManagerId( member );
        managerName = memberManagerFullName( member );
        id = member.getId();
        fullName = member.getFullName();
        displayName = member.getDisplayName();
        email = member.getEmail();
        region = member.getRegion();
        timezone = member.getTimezone();
        status = member.getStatus();
        oncallEligible = member.isOncallEligible();
        joinDate = member.getJoinDate();
        slackHandle = member.getSlackHandle();
        phone = member.getPhone();
        Set<SystemRole> set = member.getSystemRoles();
        if ( set != null ) {
            systemRoles = new LinkedHashSet<SystemRole>( set );
        }
        createdAt = member.getCreatedAt();
        updatedAt = member.getUpdatedAt();

        MemberResponse memberResponse = new MemberResponse( id, fullName, displayName, email, region, timezone, status, oncallEligible, joinDate, slackHandle, phone, managerId, managerName, systemRoles, createdAt, updatedAt );

        return memberResponse;
    }

    @Override
    public NotificationPreferenceResponse toNotificationPreferenceResponse(NotificationPreference pref) {
        if ( pref == null ) {
            return null;
        }

        UUID id = null;
        NotificationChannel channel = null;
        NotificationEventType eventType = null;
        boolean enabled = false;

        id = pref.getId();
        channel = pref.getChannel();
        eventType = pref.getEventType();
        enabled = pref.isEnabled();

        NotificationPreferenceResponse notificationPreferenceResponse = new NotificationPreferenceResponse( id, channel, eventType, enabled );

        return notificationPreferenceResponse;
    }

    private UUID memberManagerId(Member member) {
        Member manager = member.getManager();
        if ( manager == null ) {
            return null;
        }
        return manager.getId();
    }

    private String memberManagerFullName(Member member) {
        Member manager = member.getManager();
        if ( manager == null ) {
            return null;
        }
        return manager.getFullName();
    }
}
