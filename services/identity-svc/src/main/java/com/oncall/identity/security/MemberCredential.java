package com.oncall.identity.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Stores BCrypt-hashed passwords for each Member.
 * Separated from the Member entity so password hashes never leak
 * via general-purpose Member read endpoints.
 */
@Entity
@Table(name = "member_credentials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberCredential {

    /** Matches members.id — not a FK in JPA to avoid loading the full entity. */
    @Id
    @Column(columnDefinition = "uuid")
    private UUID memberId;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "password_changed_at", nullable = false)
    private Instant passwordChangedAt;
}
