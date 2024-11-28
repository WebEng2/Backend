package de.dhbw_ravensburg.webeng2.backend.controller;

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

import de.dhbw_ravensburg.webeng2.backend.service.BookInfoService;
import de.dhbw_ravensburg.webeng2.backend.model.BookInfo;

@RestController
@RequestMapping("/api/books")
public class BookController {
    @Autowired
    private BookRepository repository;
    
    @Autowired
    private BookInfoService bookInfoService;

    @GetMapping("/")
    public Page<Book> findBooks() {
        return repository.findAll(Pageable.ofSize(20));
    }

    @GetMapping("/{id}")
    public Book findById(@PathVariable String id) {
        return repository.findById(id).orElseThrow(() -> new BookNotFoundException());
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Book updateBook(
      @PathVariable("id") final String id, @RequestBody final Book book) {
        return book;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    void onIllegalArgumentException(IllegalArgumentException exception) {
        // ...
    }

    @GetMapping("/{id}/info")
    public BookInfo getBookInfo(@PathVariable String id) {
        Book book = repository.findById(id)
            .orElseThrow(() -> new BookNotFoundException());
        return bookInfoService.getBookInfo(book.getIsbn());
    }

    @GetMapping("/randominfo")
    public BookInfo getRandomBookInfo() {
        long count = repository.count();
        int randomIndex = (int) (Math.random() * count);
        Page<Book> bookPage = repository.findAll(Pageable.ofSize(1).withPage(randomIndex));
        Book randomBook = bookPage.getContent().get(0);
        return bookInfoService.getBookInfo(randomBook.getIsbn());
    }
}
