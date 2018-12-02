package com.github.pepzer.rest_jpa_test;

import com.github.pepzer.rest_jpa_test.entity.Author;
import com.github.pepzer.rest_jpa_test.entity.Book;
import com.github.pepzer.rest_jpa_test.repository.AuthorRepository;
import com.github.pepzer.rest_jpa_test.repository.BookRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application implements CommandLineRunner {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

	@Autowired
	AuthorRepository authorRepository;

	@Autowired
	BookRepository bookRepository;

	public static void main(String[] args) throws Exception {
		LOGGER.info("Running Application...");
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void run(String... args) {
		Author author = new Author("Bilbo", "Baggins");
		Book book = new Book("0123401234012", "The Hobbit");
		author.getBooks().add(book);
		book.getAuthors().add(author);
		authorRepository.save(author);
	}
}
