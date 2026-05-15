package com.codex.lms.repository;

import com.codex.lms.model.Member;
import com.codex.lms.model.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {

    long countByStatus(MemberStatus status);

    @Query("""
            select m from Member m
            where lower(m.fullName) like lower(concat('%', :term, '%'))
               or lower(m.email) like lower(concat('%', :term, '%'))
               or lower(m.membershipCode) like lower(concat('%', :term, '%'))
            order by m.fullName asc
            """)
    List<Member> search(@Param("term") String term);
}
