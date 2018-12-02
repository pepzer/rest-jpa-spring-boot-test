package com.github.pepzer.rest_jpa_test.resource;

import com.github.pepzer.rest_jpa_test.entity.Author;
import org.springframework.hateoas.ResourceSupport;

public class AuthorResource extends ResourceSupport {
    String firstName;
    String lastName;

    public AuthorResource(Author author) {
        firstName = author.getFirstName();
        lastName = author.getLastName();
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}