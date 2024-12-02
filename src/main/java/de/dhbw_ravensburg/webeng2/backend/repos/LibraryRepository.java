package de.dhbw_ravensburg.webeng2.backend.repos;

import de.dhbw_ravensburg.webeng2.backend.model.Library;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "libraries", path = "libraries", exported = false)
public interface LibraryRepository extends MongoRepository<Library, String> {

  Page<Library> findByNameContainingIgnoreCase(String name, Pageable pageable);

}