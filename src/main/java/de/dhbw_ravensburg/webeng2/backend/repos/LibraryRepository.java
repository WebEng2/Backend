package de.dhbw_ravensburg.webeng2.backend.repos;

import de.dhbw_ravensburg.webeng2.backend.model.Library;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "libraries", path = "libraries", exported = true)
public interface LibraryRepository extends MongoRepository<Library, String> {
  Optional<Library> findByName(String name);
}