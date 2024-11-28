package de.dhbw_ravensburg.webeng2.backend.repos;

import java.util.List;
import de.dhbw_ravensburg.webeng2.backend.model.Book;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.RequestMapping;

@RepositoryRestResource(collectionResourceRel = "books", path = "books")
public interface BookRepository extends MongoRepository<Book, String> {

  List<Book> findByName(@Param("name") String name);

}