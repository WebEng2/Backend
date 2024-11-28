package de.dhbw_ravensburg.webeng2.backend.repos;

import de.dhbw_ravensburg.webeng2.backend.model.Book;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "books", path = "books", exported = true)
public interface BookRepository extends MongoRepository<Book, String> {

  Page<Book> findByName(@Param("name") String name, Pageable pageable);

}