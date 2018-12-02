/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.pepzer.rest_jpa_test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.github.pepzer.rest_jpa_test.repository.AuthorRepository;
import com.github.pepzer.rest_jpa_test.repository.BookRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private AuthorRepository authorRepository;

	@Autowired
	private BookRepository bookRepository;

	@Before
	public void deleteAllBeforeTests() throws Exception {
		authorRepository.deleteAll();
		bookRepository.deleteAll();
	}

	@Test
	public void shouldReturnRepositoryIndex() throws Exception {

		mockMvc.perform(get("/")).andDo(print()).andExpect(status().isOk())
				.andExpect(jsonPath("$._links.authors").exists()).andExpect(jsonPath("$._links.books").exists());
	}

	@Test
	public void shouldCreateAuthor() throws Exception {

		mockMvc.perform(post("/authors").header(HttpHeaders.CONTENT_TYPE, "application/json")
				.content("{\"firstName\": \"Bilbo\", \"lastName\":\"Baggins\"}")).andExpect(status().isCreated())
				.andExpect(header().string("Location", containsString("authors/")));
	}

	@Test
	public void shouldCreateBook() throws Exception {

		mockMvc.perform(post("/books").header(HttpHeaders.CONTENT_TYPE, "application/json")
				.content("{\"isbn\": \"0123401234012\", \"title\":\"The Hobbit\"}")).andExpect(status().isCreated())
				.andExpect(header().string("Location", containsString("books/")));
	}

	@Test
	public void shouldRetrieveAuthor() throws Exception {

		MvcResult mvcResult = mockMvc
				.perform(post("/authors").header(HttpHeaders.CONTENT_TYPE, "application/json")
						.content("{\"firstName\": \"Bilbo\", \"lastName\":\"Baggins\"}"))
				.andExpect(status().isCreated()).andReturn();

		String location = mvcResult.getResponse().getHeader("Location");
		mockMvc.perform(get(location)).andExpect(status().isOk()).andExpect(jsonPath("$.firstName").value("Bilbo"))
				.andExpect(jsonPath("$.lastName").value("Baggins"));
	}

	@Test
	public void shouldRetrieveBook() throws Exception {

		MvcResult mvcResult = mockMvc
				.perform(post("/books").header(HttpHeaders.CONTENT_TYPE, "application/json")
						.content("{\"isbn\": \"0123401234012\", \"title\":\"The Hobbit\"}"))
				.andExpect(status().isCreated()).andReturn();

		String location = mvcResult.getResponse().getHeader("Location");
		mockMvc.perform(get(location)).andExpect(status().isOk()).andExpect(jsonPath("$.isbn").value("0123401234012"))
				.andExpect(jsonPath("$.title").value("The Hobbit"));
	}

	@Test
	public void shouldRetrieveAuthorBook() throws Exception {
		// Insert an author
		MvcResult authorResult = mockMvc
				.perform(post("/authors").header(HttpHeaders.CONTENT_TYPE, "application/json")
						.content("{\"firstName\": \"Bilbo\", \"lastName\":\"Baggins\"}"))
				.andExpect(status().isCreated()).andReturn();

		// Insert a book
		MvcResult bookResult = mockMvc
				.perform(post("/books").header(HttpHeaders.CONTENT_TYPE, "application/json")
						.content("{\"isbn\": \"0123401234012\", \"title\":\"The Hobbit\"}"))
				.andExpect(status().isCreated()).andReturn();

		String bookLocation = bookResult.getResponse().getHeader("Location");
		String[] splitBookLoc = bookLocation.split("/");
		String bookId = splitBookLoc[splitBookLoc.length - 1];
		String authorLocation = authorResult.getResponse().getHeader("Location");
		String putLocation = String.format("%s/books/%s", authorLocation, bookId);
		// Assign a book to an author through a PUT to
		// /authors/{author-id}/books/{book-id}
		MvcResult putResult = mockMvc.perform(put(putLocation)).andExpect(status().isCreated()).andReturn();

		// PUT returns Location: /authors/{author-id}/books
		String location = putResult.getResponse().getHeader("Location");
		// GET the location to see if the added book is listed
		mockMvc.perform(get(location)).andExpect(status().isOk())
				.andExpect(jsonPath("$._embedded.books[0].isbn").value("0123401234012"))
				.andExpect(jsonPath("$._embedded.books[0].title").value("The Hobbit"));

		// The author should be listed through a GET to /books/{book-id}/authors
		mockMvc.perform(get(bookLocation + "/authors")).andExpect(status().isOk())
				.andExpect(jsonPath("$._embedded.authors[0].firstName").value("Bilbo"))
				.andExpect(jsonPath("$._embedded.authors[0].lastName").value("Baggins"));
	}

	@Test
	public void shouldRetrieveBookAuthor() throws Exception {
		// Like the previous test but PUT on /books/{book-id}/authors/{author-id}

		// Insert an author
		MvcResult authorResult = mockMvc
				.perform(post("/authors").header(HttpHeaders.CONTENT_TYPE, "application/json")
						.content("{\"firstName\": \"Bilbo\", \"lastName\":\"Baggins\"}"))
				.andExpect(status().isCreated()).andReturn();

		// Insert a book
		MvcResult bookResult = mockMvc
				.perform(post("/books").header(HttpHeaders.CONTENT_TYPE, "application/json")
						.content("{\"isbn\": \"0123401234012\", \"title\":\"The Hobbit\"}"))
				.andExpect(status().isCreated()).andReturn();

		String bookLocation = bookResult.getResponse().getHeader("Location");
		String authorLocation = authorResult.getResponse().getHeader("Location");
		String[] splitAuthorLoc = authorLocation.split("/");
		String authorId = splitAuthorLoc[splitAuthorLoc.length - 1];
		String putLocation = String.format("%s/authors/%s", bookLocation, authorId);
		// Assign an author to a book through a PUT to
		// /books/{book-id}/authors/{author-id}
		MvcResult putResult = mockMvc.perform(put(putLocation)).andExpect(status().isCreated()).andReturn();

		// PUT returns Location: /books/{book-id}/authors
		String location = putResult.getResponse().getHeader("Location");
		// GET the location to see if the added author is listed
		mockMvc.perform(get(location)).andExpect(status().isOk())
				.andExpect(jsonPath("$._embedded.authors[0].firstName").value("Bilbo"))
				.andExpect(jsonPath("$._embedded.authors[0].lastName").value("Baggins"));

		// The book should be listed through a GET to /authors/{author-id}/books
		mockMvc.perform(get(authorLocation + "/books")).andExpect(status().isOk())
				.andExpect(jsonPath("$._embedded.books[0].isbn").value("0123401234012"))
				.andExpect(jsonPath("$._embedded.books[0].title").value("The Hobbit"));
	}

	@Test
	public void shouldQueryAuthor() throws Exception {

		mockMvc.perform(post("/authors").header(HttpHeaders.CONTENT_TYPE, "application/json")
				.content("{\"firstName\": \"Bilbo\", \"lastName\":\"Baggins\"}")).andExpect(status().isCreated())
				.andReturn();

		mockMvc.perform(get("/authors/search/findByLastName?lastName={lastName}", "Baggins")).andExpect(status().isOk())
				.andExpect(jsonPath("$._embedded.authorResources[0].firstName").value("Bilbo"));
	}

	@Test
	public void shouldQueryBook() throws Exception {

		mockMvc.perform(post("/books").header(HttpHeaders.CONTENT_TYPE, "application/json")
				.content("{\"isbn\": \"0123401234012\", \"title\":\"The Hobbit\"}")).andExpect(status().isCreated())
				.andReturn();

		mockMvc.perform(get("/books/search/findByIsbn?isbn={isbn}", "0123401234012")).andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("The Hobbit"));
	}

	@Test
	public void shouldDeleteAuthor() throws Exception {

		MvcResult mvcResult = mockMvc
				.perform(post("/authors").header(HttpHeaders.CONTENT_TYPE, "application/json")
						.content("{\"firstName\": \"Bilbo\", \"lastName\":\"Baggins\"}"))
				.andExpect(status().isCreated()).andReturn();

		String location = mvcResult.getResponse().getHeader("Location");
		mockMvc.perform(delete(location)).andExpect(status().isNoContent());

		// Check the author is deleted
		mockMvc.perform(get(location)).andExpect(status().isNotFound());
	}

	@Test
	public void shouldDeleteBook() throws Exception {
		// Insert a book
		MvcResult mvcResult = mockMvc
				.perform(post("/books").header(HttpHeaders.CONTENT_TYPE, "application/json")
						.content("{\"isbn\": \"0123401234012\", \"title\":\"The Hobbit\"}"))
				.andExpect(status().isCreated()).andReturn();

		String location = mvcResult.getResponse().getHeader("Location");
		mockMvc.perform(delete(location)).andExpect(status().isNoContent());

		mockMvc.perform(get(location)).andExpect(status().isNotFound());
	}
}