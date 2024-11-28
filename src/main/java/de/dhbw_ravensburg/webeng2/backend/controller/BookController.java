package de.dhbw_ravensburg.webeng2.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import de.dhbw_ravensburg.webeng2.backend.model.Book;
import de.dhbw_ravensburg.webeng2.backend.repos.BookRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import org.springframework.web.bind.annotation.PostMapping;

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
    @Operation(summary = "Get all Books", description = "Retrieves a paginated and optionally sorted list of books.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved books"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters provided")
    })
    public Page<Book> findBooks(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort fields") @RequestParam(defaultValue = "") String[] sort) {
        return repository.findAll(PageRequest.of(page, size, Sort.by(sort)));
    }

    @PostMapping("/")
    @Operation(summary = "Create new Book", description = "Creates a new book.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created a new book"),
            @ApiResponse(responseCode = "400", description = "Invalid book data provided"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Book postBook(
            @Parameter(description = "The book to be added to the repository") @RequestParam Book book) {
        return repository.insert(book);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Book by ID", description = "Retrieves a book")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved book"),
            @ApiResponse(responseCode = "400", description = "Invalid book data provided"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Book findById(
            @Parameter(description = "The internal id of the book") @RequestParam @PathVariable String id) {
        return repository.findById(id).orElseThrow(() -> new BookException("Unknown ID"));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Book updateBook(
            @PathVariable("id") final String id, @RequestBody final Book book) {
        return book;
    }

    @GetMapping("findByName/{name}")
    public Page<Book> findByName(@PathVariable String name) {
        return repository.findByName(name, Pageable.ofSize(20));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> onIllegalArgumentException(IllegalArgumentException ex) {
        return new ResponseEntity<>(ErrorResponse.create(ex, HttpStatus.BAD_REQUEST, ex.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> onConstraintViolationException(ConstraintViolationException ex) {
        return new ResponseEntity<>(ErrorResponse.create(ex, HttpStatus.BAD_REQUEST, ex.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BookException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> onBookException(BookException ex) {
        return new ResponseEntity<>(ErrorResponse.create(ex, HttpStatus.BAD_REQUEST, ex.getMessage()),
                HttpStatus.BAD_REQUEST);
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
