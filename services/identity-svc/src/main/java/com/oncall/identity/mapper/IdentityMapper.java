package com.oncall.identity.mapper;

import com.oncall.domain.entity.Member;
import com.oncall.domain.entity.NotificationPreference;
import com.oncall.identity.dto.response.MemberResponse;
import com.oncall.identity.dto.response.NotificationPreferenceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper — works with Java records out of the box.
 * For record targets, MapStruct generates code that calls the canonical constructor.
 * Source Member entity uses Lombok getters (getId(), getEmail(), etc.).
 */
@Mapper(componentModel = "spring")
public interface IdentityMapper {

    @Mapping(target = "managerId",   source = "manager.id")
    @Mapping(target = "managerName", source = "manager.fullName")
    MemberResponse toMemberResponse(Member member);

    NotificationPreferenceResponse toNotificationPreferenceResponse(NotificationPreference pref);
}
