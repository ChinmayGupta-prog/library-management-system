package com.codex.lms.controller;

import com.codex.lms.model.BookStatus;
import com.codex.lms.repository.BookRepository;
import com.codex.lms.repository.LoanRepository;
import com.codex.lms.repository.MemberRepository;
import com.codex.lms.service.LibraryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/loans")
public class LoanController {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final LibraryService libraryService;

    public LoanController(LoanRepository loanRepository,
                          BookRepository bookRepository,
                          MemberRepository memberRepository,
                          LibraryService libraryService) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.libraryService = libraryService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("loans", loanRepository.findAllDetailed());
        return "loans/list";
    }

    @GetMapping("/new")
    public String issueForm(@RequestParam(required = false) Long bookId, Model model) {
        model.addAttribute("books", bookRepository.findByStatusOrderByTitleAsc(BookStatus.AVAILABLE));
        model.addAttribute("members", memberRepository.findAll());
        model.addAttribute("selectedBookId", bookId);
        return "loans/form";
    }

    @PostMapping
    public String issue(@RequestParam Long bookId,
                        @RequestParam Long memberId,
                        @RequestParam(defaultValue = "14") int days,
                        RedirectAttributes redirectAttributes) {
        try {
            libraryService.issueBook(bookId, memberId, days);
            redirectAttributes.addFlashAttribute("success", "Book issued successfully.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/loans";
    }

    @PostMapping("/{id}/return")
    public String returnBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        libraryService.returnLoan(id);
        redirectAttributes.addFlashAttribute("success", "Book returned and marked available.");
        return "redirect:/loans";
    }
}
