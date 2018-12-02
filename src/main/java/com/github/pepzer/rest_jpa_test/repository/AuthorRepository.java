package com.github.pepzer.rest_jpa_test.repository;

import com.github.pepzer.rest_jpa_test.entity.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AuthorRepository extends JpaRepository<Author, Long> {
    @Query(value = "SELECT * FROM authors WHERE lastname = ?1", countQuery = "SELECT count(*) FROM authors WHERE lastname = ?1", nativeQuery = true)
    Page<Author> findByLastName(String lastName, Pageable pageable);
}