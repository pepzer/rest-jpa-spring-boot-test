## About this project

This sample project implements a REST/HATEOAS service with [Spring Boot](https://github.com/spring-projects/spring-boot) using JPA for persistence.

Like this official [Spring example](https://spring.io/guides/gs/accessing-data-rest/) it uses JPA 
with an embedded H2 database, unlike the spring.io example the "DB" is composed of two entities 
(Author, Book) with a many-to-many relationship between them.

Also the methods exposed for each Repository are customized (unlike the spring.io example).

## How to run

To build and run tests (add --info for details):

```
 $ ./gradlew build
```

To build and run a local server that will listen on port 8080 by default, run:

```
 $ ./gradlew bootRun
```

## Endpoints

There are two main endpoints '/authors', '/books' that will produce a paged JSON reply of the form:

```
GET http://localhost:8080/authors

{
  "_embedded" : {
    "authorResources" : [ {
      "firstName" : "Name1",
      "lastName" : "Surname1",
      "_links" : {
        "self" : {
          "href" : "http://localhost:8080/authors/1"
        },
        "books" : {
          "href" : "http://localhost:8080/authors/1/books"
        }
      }
    }, ...]
  },
  "page" : {
    "size" : 20,
    "totalElements" : 5,
    "totalPages" : 1,
    "number" : 0
  }
```

A GET to '/authors/1/books' will list all books written by author with id=1.

A GET to '/books/2/authors' will list all authors of a book with id=2.

Entities could be inserted with a POST to the endpoints '/authors' and '/books' (eg. with curl):

```
$ curl -i -X POST -H "Content-Type:application/json" -d '{"firstName": "John", "lastName": "Smith"}' http://localhost:808/authors

$ curl -i -X POST -H "Content-Type:application/json" -d '{"isbn": "0123012301230", "title": "Book Title"}' http://localhost:8080/books
```

An Author and a Book could be connected on either endpoint by id with a PUT like:

```
$ curl -i -X PUT "http://localhost:8080/authors/1/books/2"

 or

$ curl -i -X PUT "http://localhost:8080/books/2/authors/1"
```

To delete entities:

```
$ curl -i -X DELETE "http://localhost:8080/authors/1"

$ curl -i -X DELETE "http://localhost:8080/books/2"
```

Two queries are available:

```
GET /authors/search/findByLastName?lastName={lastName}

GET /books/search/findByIsbn?isbn={isbn}
```

## Contacts

[Giuseppe Zerbo](https://github.com/pepzer), [giuseppe (dot) zerbo (at) gmail (dot) com](mailto:giuseppe.zerbo@gmail.com).
