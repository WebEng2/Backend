package de.dhbw_ravensburg.webeng2.backend.repos;

import de.dhbw_ravensburg.webeng2.backend.model.Book;

import org.springframework.data.domain.Pageable;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "books", path = "books", exported = false)
public interface BookRepository extends MongoRepository<Book, String> {

  Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);

  Optional<Book> findByIsbn(String isbn);

}