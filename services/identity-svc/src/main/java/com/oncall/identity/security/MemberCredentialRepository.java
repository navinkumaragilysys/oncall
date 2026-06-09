package com.oncall.identity.security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemberCredentialRepository extends JpaRepository<MemberCredential, UUID> {
    Optional<MemberCredential> findByMemberId(UUID memberId);
}
