package de.dhbw_ravensburg.webeng2.backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.lang.NonNull;

import de.dhbw_ravensburg.webeng2.backend.model.Book;
import de.dhbw_ravensburg.webeng2.backend.repos.BookRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

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

    // #region GET all Books
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
    // #endregion

    // #region POST new Book
    @PostMapping("/")
    @Operation(summary = "Create new Book", description = "Creates a new book.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created a new book"),
            @ApiResponse(responseCode = "400", description = "Invalid book data provided"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Book> postBook(
            @Parameter(description = "The book to be added to the repository") @Valid @RequestBody RequestEntity<Book> book) {
        // Save the new book to MongoDB
        Book b = book.getBody();
        if (b == null) {
            throw new BookException("Can't convert input to Book");
        }
        Book savedBook = repository.save(b);
        // Return the saved book with a 201 status code
        return new ResponseEntity<>(savedBook, HttpStatus.CREATED);
    }
    // #endregion

    // #region GET Book by ID
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
    // #endregion

    // #region PATCH Book by ID
    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Book updateBook(
            @PathVariable("id") final String id, @RequestBody final Book book) {
        return book;
    }
    // #endregion

    // #region GET find books by name
    @GetMapping("findByName/{name}")
    public Page<Book> findByName(@PathVariable String name) {
        return repository.findByName(name, Pageable.ofSize(20));
    }
    // #endregion

    // #region GET book info by ID
    @GetMapping("/{id}/info")
    @Operation(summary = "Get Book Info", description = "Retrieves additional information for a specific book by its ID from google and openbook api")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved book information"),
            @ApiResponse(responseCode = "404", description = "Book not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public BookInfo getBookInfo(
            @Parameter(description = "The ID of the book to get information for") @PathVariable String id) {
        Book book = repository.findById(id)
                .orElseThrow(() -> new BookException("Book not found"));
        return bookInfoService.getBookInfo(book.getIsbn());
    }
    // #endregion

    // #region GET random book info
    @GetMapping("/randominfo")
    @Operation(summary = "Get Random Book Info", description = "Retrieves additional information for a randomly selected book from google and openbook api")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved random book information"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public BookInfo getRandomBookInfo() {
        long count = repository.count();
        int randomIndex = (int) (Math.random() * count);
        Page<Book> bookPage = repository.findAll(Pageable.ofSize(1).withPage(randomIndex));
        Book randomBook = bookPage.getContent().get(0);
        return bookInfoService.getBookInfo(randomBook.getIsbn());
    }
    // #endregion

    // #region Exceptions
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> onMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        return new ResponseEntity<>(ErrorResponse.create(ex, HttpStatus.BAD_REQUEST, ex.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(com.mongodb.MongoWriteException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> onMongoWriteException(com.mongodb.MongoWriteException ex) {
        return new ResponseEntity<>(ErrorResponse.create(ex, HttpStatus.BAD_REQUEST, ex.getMessage()),
                HttpStatus.BAD_REQUEST);
    }
    // #endregion
}
