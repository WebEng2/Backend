package de.dhbw_ravensburg.webeng2.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import de.dhbw_ravensburg.webeng2.backend.model.Book;
import de.dhbw_ravensburg.webeng2.backend.repos.BookRepository;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/api/books/search")
public class BookSearchController {

    @Autowired
    private BookRepository repository;

    @GetMapping("findByName/{name}")
    public List<Book> findByName(@PathVariable String name) {
        return repository.findByName(name);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    void onIllegalArgumentException(IllegalArgumentException exception) {
        // ...
    }
}
