package com.github.pepzer.rest_jpa_test.resource;

import com.github.pepzer.rest_jpa_test.entity.Book;
import org.springframework.hateoas.ResourceSupport;

public class BookResource extends ResourceSupport {
    String ISBN;
    String title;

    public BookResource(Book book) {
        ISBN = book.getISBN();
        title = book.getTitle();
    }

    public String getISBN() {
        return ISBN;
    }

    public String getTitle() {
        return title;
    }
}