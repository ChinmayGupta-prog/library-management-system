package com.codex.lms.repository;

import com.codex.lms.model.Book;
import com.codex.lms.model.BookStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    long countByStatus(BookStatus status);

    @EntityGraph(attributePaths = {"author", "category"})
    List<Book> findTop6ByOrderByAddedOnDesc();

    @EntityGraph(attributePaths = {"author", "category"})
    List<Book> findByStatusOrderByTitleAsc(BookStatus status);

    @Query("""
            select b from Book b
            left join fetch b.author
            left join fetch b.category
            where lower(b.title) like lower(concat('%', :term, '%'))
               or lower(b.isbn) like lower(concat('%', :term, '%'))
               or lower(b.author.name) like lower(concat('%', :term, '%'))
               or lower(b.category.name) like lower(concat('%', :term, '%'))
            order by b.title asc
            """)
    List<Book> search(@Param("term") String term);

    @Query("""
            select b from Book b
            left join fetch b.author
            left join fetch b.category
            where b.id = :id
            """)
    Optional<Book> findDetailedById(@Param("id") Long id);

    @Query("""
            select b from Book b
            left join fetch b.author
            left join fetch b.category
            order by b.title asc
            """)
    List<Book> findAllDetailed();
}
