package de.dhbw_ravensburg.webeng2.backend.repos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import de.dhbw_ravensburg.webeng2.backend.model.Redis;


@Repository
public interface RedisRepository extends CrudRepository<Redis, String> {
    
}
