package com.codex.lms.service;

import com.codex.lms.model.Book;
import com.codex.lms.model.BookStatus;
import com.codex.lms.model.Loan;
import com.codex.lms.model.LoanStatus;
import com.codex.lms.model.Member;
import com.codex.lms.model.MemberStatus;
import com.codex.lms.repository.BookRepository;
import com.codex.lms.repository.CategoryRepository;
import com.codex.lms.repository.LoanRepository;
import com.codex.lms.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class LibraryService {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final LoanRepository loanRepository;
    private final CategoryRepository categoryRepository;

    public LibraryService(BookRepository bookRepository,
                          MemberRepository memberRepository,
                          LoanRepository loanRepository,
                          CategoryRepository categoryRepository) {
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.loanRepository = loanRepository;
        this.categoryRepository = categoryRepository;
    }

    public DashboardStats dashboardStats() {
        return new DashboardStats(
                bookRepository.count(),
                bookRepository.countByStatus(BookStatus.AVAILABLE),
                bookRepository.countByStatus(BookStatus.ISSUED),
                memberRepository.countByStatus(MemberStatus.ACTIVE),
                loanRepository.countByStatus(LoanStatus.ACTIVE),
                loanRepository.countByDueOnBeforeAndStatus(LocalDate.now(), LoanStatus.ACTIVE),
                categoryRepository.count()
        );
    }

    public List<Book> catalog(String query) {
        if (query == null || query.isBlank()) {
            return bookRepository.findAllDetailed();
        }
        return bookRepository.search(query.trim());
    }

    public List<Member> members(String query) {
        if (query == null || query.isBlank()) {
            return memberRepository.findAll()
                    .stream()
                    .sorted((left, right) -> left.getFullName().compareToIgnoreCase(right.getFullName()))
                    .toList();
        }
        return memberRepository.search(query.trim());
    }

    @Transactional
    public Loan issueBook(Long bookId, Long memberId, int days) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        if (book.getStatus() != BookStatus.AVAILABLE) {
            throw new IllegalStateException("Only available books can be issued.");
        }
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new IllegalStateException("Only active members can borrow books.");
        }

        Loan loan = new Loan();
        loan.setBook(book);
        loan.setMember(member);
        loan.setIssuedOn(LocalDate.now());
        loan.setDueOn(LocalDate.now().plusDays(Math.max(days, 1)));
        loan.setStatus(LoanStatus.ACTIVE);
        book.setStatus(BookStatus.ISSUED);

        return loanRepository.save(loan);
    }

    @Transactional
    public void returnLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));
        loan.setReturnedOn(LocalDate.now());
        loan.setStatus(LoanStatus.RETURNED);
        loan.getBook().setStatus(BookStatus.AVAILABLE);
    }
}
