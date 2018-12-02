package com.github.pepzer.rest_jpa_test.controller;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.net.URI;
import java.util.Optional;

import com.github.pepzer.rest_jpa_test.entity.Author;
import com.github.pepzer.rest_jpa_test.entity.Book;
import com.github.pepzer.rest_jpa_test.repository.AuthorRepository;
import com.github.pepzer.rest_jpa_test.repository.BookRepository;
import com.github.pepzer.rest_jpa_test.resource.AuthorResource;

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
public class AuthorController {

    @Autowired
    AuthorRepository repository;

    @Autowired
    BookRepository bookRepository;

    @GetMapping("/authors")
    HttpEntity<PagedResources<AuthorResource>> getAuthors(Pageable pageable, PagedResourcesAssembler assembler) {

        Page<Author> authors = repository.findAll(pageable);
        Page<AuthorResource> authorsRes = authors.map(author -> {
            AuthorResource authorRes = new AuthorResource(author);
            ControllerLinkBuilder linkTo = linkTo(methodOn(this.getClass()).getAuthors(pageable, assembler))
                    .slash(author.getId());
            authorRes.add(linkTo.withSelfRel());
            authorRes.add(linkTo.slash("books").withRel("books"));
            return authorRes;
        });
        ControllerLinkBuilder linkTo = linkTo(methodOn(this.getClass()).getAuthorsByName("", pageable, assembler));

        PagedResources<AuthorResource> pagedRes = assembler.toResource(authorsRes);
        Link link = new Link(linkTo.withSelfRel().getHref() + "{?lastName,page,size,sort}", "search");
        pagedRes.add(link);

        return new ResponseEntity<>(pagedRes, HttpStatus.OK);
    }

    @GetMapping("/authors/{id}")
    HttpEntity<AuthorResource> getAuthor(@PathVariable Long id) {

        Optional<Author> author = repository.findById(id);
        if (author.isPresent()) {
            AuthorResource authorRes = new AuthorResource(author.get());
            ControllerLinkBuilder linkTo = linkTo(methodOn(this.getClass()).getAuthor(author.get().getId()));
            authorRes.add(linkTo.withSelfRel());
            authorRes.add(linkTo.slash("books").withRel("books"));
            return new ResponseEntity<>(authorRes, HttpStatus.OK);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/authors/search/findByLastName")
    HttpEntity<PagedResources<AuthorResource>> getAuthorsByName(@Param("lastName") String lastName, Pageable pageable,
            PagedResourcesAssembler assembler) {

        Page<Author> authors = repository.findByLastName(lastName, pageable);

        Page<AuthorResource> authorsRes = authors.map(author -> {
            AuthorResource authorRes = new AuthorResource(author);
            ControllerLinkBuilder linkTo = linkTo(methodOn(this.getClass()).getAuthors(pageable, assembler))
                    .slash(author.getId());
            authorRes.add(linkTo.withSelfRel());
            authorRes.add(linkTo.slash("books").withRel("books"));
            return authorRes;
        });

        return new ResponseEntity<>(assembler.toResource(authorsRes), HttpStatus.OK);
    }

    @PostMapping("/authors")
    public ResponseEntity<Object> createAuthor(@RequestBody Author author) {
        Author savedAuthor = repository.save(author);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(savedAuthor.getId()).toUri();

        return ResponseEntity.created(location).build();
    }

    @PutMapping("/authors/{author-id}/books/{book-id}")
    public ResponseEntity<Object> addAuthorBook(@PathVariable("author-id") Long authorId,
            @PathVariable("book-id") Long bookId) {

        Optional<Author> author = repository.findById(authorId);
        if (author.isPresent()) {
            Optional<Book> book = bookRepository.findById(bookId);
            if (book.isPresent()) {
                Author saveAuthor = author.get();
                saveAuthor.getBooks().add(book.get());
                repository.save(saveAuthor);
                ControllerLinkBuilder linkTo = linkTo(methodOn(this.getClass()).getAuthors(null, null))
                        .slash(saveAuthor.getId()).slash("books");
                return ResponseEntity.created(linkTo.toUri()).build();
            }
        }
        return ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/authors/{id}")
    public ResponseEntity<Object> deleteAuthor(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}