package com.codex.lms.repository;

import com.codex.lms.model.Loan;
import com.codex.lms.model.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    long countByStatus(LoanStatus status);

    long countByDueOnBeforeAndStatus(LocalDate dueOn, LoanStatus status);

    @Query("""
            select l from Loan l
            join fetch l.book b
            left join fetch b.author
            join fetch l.member
            order by l.issuedOn desc
            """)
    List<Loan> findAllDetailed();

    @Query("""
            select l from Loan l
            join fetch l.book b
            left join fetch b.author
            join fetch l.member
            where l.status = com.codex.lms.model.LoanStatus.ACTIVE
            order by l.dueOn asc
            """)
    List<Loan> findActiveDetailed();
}
