package com.codex.lms.controller;

import com.codex.lms.model.BookStatus;
import com.codex.lms.repository.BookRepository;
import com.codex.lms.repository.LoanRepository;
import com.codex.lms.service.LibraryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final LibraryService libraryService;
    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;

    public HomeController(LibraryService libraryService,
                          BookRepository bookRepository,
                          LoanRepository loanRepository) {
        this.libraryService = libraryService;
        this.bookRepository = bookRepository;
        this.loanRepository = loanRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("stats", libraryService.dashboardStats());
        model.addAttribute("newBooks", bookRepository.findTop6ByOrderByAddedOnDesc());
        model.addAttribute("availableBooks", bookRepository.findByStatusOrderByTitleAsc(BookStatus.AVAILABLE));
        model.addAttribute("activeLoans", loanRepository.findActiveDetailed());
        return "home";
    }
}
