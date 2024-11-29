package de.dhbw_ravensburg.webeng2.backend.repos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import de.dhbw_ravensburg.webeng2.backend.model.BookInfo;

@RepositoryRestResource(collectionResourceRel = "bookInfos", path = "bookInfos", exported = false)
public interface RedisRepository extends CrudRepository<BookInfo, String> {

}
