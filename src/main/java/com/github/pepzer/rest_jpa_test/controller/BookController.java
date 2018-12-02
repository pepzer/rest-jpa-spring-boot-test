package com.github.pepzer.rest_jpa_test.controller;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.net.URI;
import java.util.Optional;

import com.github.pepzer.rest_jpa_test.entity.Author;
import com.github.pepzer.rest_jpa_test.entity.Book;
import com.github.pepzer.rest_jpa_test.repository.AuthorRepository;
import com.github.pepzer.rest_jpa_test.repository.BookRepository;
import com.github.pepzer.rest_jpa_test.resource.BookResource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
public class BookController {

    @Autowired
    BookRepository repository;

    @Autowired
    AuthorRepository authorRepo;

    @GetMapping("/books")
    HttpEntity<PagedResources<BookResource>> getBooks(Pageable pageable, PagedResourcesAssembler assembler) {

        Page<Book> books = repository.findAll(pageable);
        Page<BookResource> booksRes = books.map(book -> {
            BookResource bookRes = new BookResource(book);
            ControllerLinkBuilder linkTo = linkTo(methodOn(this.getClass()).getBooks(pageable, assembler))
                    .slash(book.getId());
            bookRes.add(linkTo.withSelfRel());
            bookRes.add(linkTo.slash("authors").withRel("authors"));
            return bookRes;
        });
        ControllerLinkBuilder linkTo = linkTo(methodOn(this.getClass()).getBooksByIsbn(""));

        PagedResources<BookResource> pagedRes = assembler.toResource(booksRes);
        Link link = new Link(linkTo.withSelfRel().getHref() + "{?isbn}", "search");
        pagedRes.add(link);
        return new ResponseEntity<>(pagedRes, HttpStatus.OK);
    }

    @GetMapping("/books/{id}")
    HttpEntity<BookResource> getBook(@PathVariable Long id) {

        Optional<Book> book = repository.findById(id);
        if (book.isPresent()) {
            BookResource bookRes = new BookResource(book.get());
            ControllerLinkBuilder linkTo = linkTo(methodOn(this.getClass()).getBook(book.get().getId()));
            bookRes.add(linkTo.withSelfRel());
            bookRes.add(linkTo.slash("authors").withRel("authors"));
            return new ResponseEntity<>(bookRes, HttpStatus.OK);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/books/search/findByIsbn")
    HttpEntity<BookResource> getBooksByIsbn(@Param("isbn") String isbn) {

        Optional<Book> book = repository.findByIsbn(isbn);
        if (book.isPresent()) {
            BookResource bookRes = new BookResource(book.get());
            ControllerLinkBuilder linkTo = linkTo(methodOn(this.getClass()).getBooks(null, null))
                    .slash(book.get().getId());
            bookRes.add(linkTo.withSelfRel());
            bookRes.add(linkTo.slash("authors").withRel("authors"));

            return new ResponseEntity<>(bookRes, HttpStatus.OK);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/books")
    public ResponseEntity<Object> createStudent(@RequestBody Book book) {
        Book savedBook = repository.save(book);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(savedBook.getId())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @PutMapping("/books/{book-id}/authors/{author-id}")
    public ResponseEntity<Object> addBookAuthor(@PathVariable("book-id") Long bookId,
            @PathVariable("author-id") Long authorId) {

        Optional<Author> author = authorRepo.findById(authorId);
        if (author.isPresent()) {
            Optional<Book> book = repository.findById(bookId);
            if (book.isPresent()) {
                Author saveAuthor = author.get();
                saveAuthor.getBooks().add(book.get());
                authorRepo.save(saveAuthor);
                ControllerLinkBuilder linkTo = linkTo(methodOn(this.getClass()).getBooks(null, null))
                        .slash(book.get().getId()).slash("authors");
                return ResponseEntity.created(linkTo.toUri()).build();
            }
        }
        return ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<Object> deleteBook(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}