package com.codex.lms.config;

import com.codex.lms.model.Author;
import com.codex.lms.model.Book;
import com.codex.lms.model.BookStatus;
import com.codex.lms.model.Category;
import com.codex.lms.model.Member;
import com.codex.lms.repository.AuthorRepository;
import com.codex.lms.repository.BookRepository;
import com.codex.lms.repository.CategoryRepository;
import com.codex.lms.repository.MemberRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;

    public DataSeeder(AuthorRepository authorRepository,
                      BookRepository bookRepository,
                      CategoryRepository categoryRepository,
                      MemberRepository memberRepository) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    public void run(String... args) {
        if (bookRepository.count() > 0) {
            return;
        }

        Category technology = categoryRepository.save(new Category("Technology", "Architecture, programming, cloud and data systems.", "#35d0ba"));
        Category design = categoryRepository.save(new Category("Design", "Experience design, typography and product thinking.", "#ffb86b"));
        Category business = categoryRepository.save(new Category("Business", "Leadership, operations and strategic decision-making.", "#7c9cff"));
        Category fiction = categoryRepository.save(new Category("Fiction", "Literary fiction and modern classics.", "#ff6f91"));

        Author martin = authorRepository.save(new Author("Robert C. Martin", "United States", "Software craftsman and author focused on clean code practices."));
        Author kleppmann = authorRepository.save(new Author("Martin Kleppmann", "United Kingdom", "Distributed systems researcher and data platform author."));
        Author norman = authorRepository.save(new Author("Don Norman", "United States", "Design researcher known for human-centered product thinking."));
        Author morgenstern = authorRepository.save(new Author("Erin Morgenstern", "United States", "Novelist known for immersive literary fantasy."));
        Author collins = authorRepository.save(new Author("Jim Collins", "United States", "Researcher and author on durable business performance."));

        bookRepository.saveAll(List.of(
                book("Clean Architecture", "9780134494166", martin, technology, "Prentice Hall", 2017, "A-14", "https://images.unsplash.com/photo-1515879218367-8466d910aaa4?auto=format&fit=crop&w=600&q=80", "A pragmatic guide to designing maintainable software systems."),
                book("Designing Data-Intensive Applications", "9781449373320", kleppmann, technology, "O'Reilly Media", 2017, "A-22", "https://images.unsplash.com/photo-1558494949-ef010cbdcc31?auto=format&fit=crop&w=600&q=80", "A deep look at databases, streams, distributed systems and reliability."),
                book("The Design of Everyday Things", "9780465050659", norman, design, "Basic Books", 2013, "D-03", "https://images.unsplash.com/photo-1518005020951-eccb494ad742?auto=format&fit=crop&w=600&q=80", "A classic introduction to usable, human-centered design."),
                book("The Night Circus", "9780307744432", morgenstern, fiction, "Vintage", 2011, "F-18", "https://images.unsplash.com/photo-1519682337058-a94d519337bc?auto=format&fit=crop&w=600&q=80", "A lush novel about a magical competition unfolding inside a mysterious circus."),
                book("Good to Great", "9780066620992", collins, business, "HarperBusiness", 2001, "B-09", "https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?auto=format&fit=crop&w=600&q=80", "Research-backed ideas for building companies with enduring excellence.")
        ));

        Member aisha = member("Aisha Mehra", "aisha.mehra@example.com", "+91 98765 43210", "LMS-1001");
        Member rohan = member("Rohan Kapoor", "rohan.kapoor@example.com", "+91 87654 32109", "LMS-1002");
        Member neha = member("Neha Sharma", "neha.sharma@example.com", "+91 76543 21098", "LMS-1003");
        memberRepository.saveAll(List.of(aisha, rohan, neha));
    }

    private Book book(String title, String isbn, Author author, Category category, String publisher,
                      int year, String shelf, String coverUrl, String summary) {
        Book book = new Book();
        book.setTitle(title);
        book.setIsbn(isbn);
        book.setAuthor(author);
        book.setCategory(category);
        book.setPublisher(publisher);
        book.setPublicationYear(year);
        book.setShelfLocation(shelf);
        book.setCoverUrl(coverUrl);
        book.setSummary(summary);
        book.setStatus(BookStatus.AVAILABLE);
        book.setAddedOn(LocalDate.now().minusDays((long) (Math.random() * 30)));
        return book;
    }

    private Member member(String name, String email, String phone, String code) {
        Member member = new Member();
        member.setFullName(name);
        member.setEmail(email);
        member.setPhone(phone);
        member.setMembershipCode(code);
        return member;
    }
}
