package de.dhbw_ravensburg.webeng2.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import de.dhbw_ravensburg.webeng2.backend.model.Library;
import de.dhbw_ravensburg.webeng2.backend.repos.LibraryRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/libraries")
public class LibraryController {

    @Autowired
    private LibraryRepository repository;

    @PostMapping
    public ResponseEntity<Library> createLibrary(@RequestBody Library library) {
        Library savedLibrary = repository.save(library);
        return new ResponseEntity<>(savedLibrary, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Library> getLibraryById(@PathVariable String id) {
        Optional<Library> library = repository.findById(id);
        return library.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Library> getLibraryByName(@PathVariable String name) {
        Optional<Library> library = repository.findByName(name);
        return library.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/")
    public List<Library> getAllLibraries() {
        return repository.findAll();
    }
}
