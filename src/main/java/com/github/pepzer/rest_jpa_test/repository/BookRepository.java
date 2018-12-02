package com.github.pepzer.rest_jpa_test.repository;

import java.util.Optional;
import com.github.pepzer.rest_jpa_test.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookRepository extends JpaRepository<Book, Long> {
    @Query(value = "SELECT b FROM Book b WHERE b.ISBN = ?1")
    Optional<Book> findByIsbn(String isbn);
}