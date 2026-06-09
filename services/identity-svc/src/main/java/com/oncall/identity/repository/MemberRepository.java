package com.oncall.identity.repository;

import com.oncall.domain.entity.Member;
import com.oncall.domain.enums.MemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemberRepository extends JpaRepository<Member, UUID> {

    Optional<Member> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    Page<Member> findByStatus(MemberStatus status, Pageable pageable);

    @Query("SELECT m FROM Member m WHERE m.manager.id = :managerId")
    Page<Member> findDirectReports(@Param("managerId") UUID managerId, Pageable pageable);

    @Query("SELECT m FROM Member m WHERE m.oncallEligible = true AND m.status = 'ACTIVE'")
    Page<Member> findEligibleMembers(Pageable pageable);
}
