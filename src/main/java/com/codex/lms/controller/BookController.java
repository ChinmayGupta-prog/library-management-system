package com.codex.lms.controller;

import com.codex.lms.model.Author;
import com.codex.lms.model.Book;
import com.codex.lms.model.BookStatus;
import com.codex.lms.repository.AuthorRepository;
import com.codex.lms.repository.BookRepository;
import com.codex.lms.repository.CategoryRepository;
import com.codex.lms.service.LibraryService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/books")
public class BookController {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final LibraryService libraryService;

    public BookController(BookRepository bookRepository,
                          AuthorRepository authorRepository,
                          CategoryRepository categoryRepository,
                          LibraryService libraryService) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
        this.libraryService = libraryService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("books", libraryService.catalog(q));
        model.addAttribute("query", q);
        return "books/list";
    }

    @GetMapping("/new")
    public String create(Model model) {
        model.addAttribute("book", new Book());
        addFormData(model);
        return "books/form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("book", bookRepository.findDetailedById(id).orElseThrow());
        addFormData(model);
        return "books/form";
    }

    @PostMapping
    public String save(@Valid @ModelAttribute Book book,
                       BindingResult result,
                       @RequestParam String authorName,
                       @RequestParam Long categoryId,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (authorName == null || authorName.isBlank()) {
            result.rejectValue("author", "author.required", "Author name is required.");
        }
        if (result.hasErrors()) {
            addFormData(model);
            return "books/form";
        }
        if (book.getId() != null) {
            bookRepository.findById(book.getId()).ifPresent(existing -> book.setAddedOn(existing.getAddedOn()));
        } else if (book.getAddedOn() == null) {
            book.setAddedOn(LocalDate.now());
        }
        String cleanAuthorName = authorName.trim();
        Author author = authorRepository.findByNameIgnoreCase(cleanAuthorName)
                .orElseGet(() -> authorRepository.save(new Author(cleanAuthorName, "", "")));
        book.setAuthor(author);
        book.setCategory(categoryRepository.findById(categoryId).orElseThrow());
        bookRepository.save(book);
        redirectAttributes.addFlashAttribute("success", "Book saved successfully.");
        return "redirect:/books";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        bookRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Book removed from catalog.");
        return "redirect:/books";
    }

    private void addFormData(Model model) {
        model.addAttribute("authors", authorRepository.findAll());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("statuses", BookStatus.values());
    }
}
