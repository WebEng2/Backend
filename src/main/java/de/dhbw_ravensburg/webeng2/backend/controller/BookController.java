package de.dhbw_ravensburg.webeng2.backend.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;

import de.dhbw_ravensburg.webeng2.backend.service.BookInfoService;
import de.dhbw_ravensburg.webeng2.backend.model.BookInfo;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = { RequestMethod.GET,
        RequestMethod.POST,
        RequestMethod.PUT })
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
            @ApiResponse(responseCode = "400", description = "Invalid parameters provided"),
            @ApiResponse(responseCode = "404", description = "No books in Database")
    })
    public ResponseEntity<Page<Book>> findBooks(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort fields") @RequestParam(defaultValue = "") String[] sort) {
        Page<Book> books = repository.findAll(PageRequest.of(page, size, Sort.by(sort)));
        if (books.isEmpty()) {
            // If no books are found, return a 404 Not Found
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Return the list of books with a 200 OK status
        return new ResponseEntity<>(books, HttpStatus.OK);
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

    // #region PUT Update Book by ID
    @PutMapping("/{id}")
    @Operation(summary = "Update a Book", description = "Updates an existing book.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated book"),
            @ApiResponse(responseCode = "400", description = "Invalid book data provided"),
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "Book does not exist")
    })
    public ResponseEntity<Book> updateBook(
            @Parameter(description = "The id of the book to edid") @PathVariable("id") String id,
            @Parameter(description = "The book to be added to the repository") @Valid @RequestBody RequestEntity<Book> book) {
        // Check if the book exists in the database
        Optional<Book> existingBookOptional = repository.findById(id);

        if (!existingBookOptional.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Return 404 if the book doesn't exist
        }
        Book b = book.getBody();
        if (b == null) {
            throw new BookException("Can't convert input to Book");
        }

        // Get the existing book
        Book existingBook = existingBookOptional.get();

        // Update the fields of the existing book
        existingBook.setIsbn(b.getIsbn());
        existingBook.setTitle(b.getTitle());

        // Save the updated book
        Book updatedBook = repository.save(existingBook);

        // Return the updated book with a 200 OK status code
        return new ResponseEntity<>(updatedBook, HttpStatus.OK);
    }
    // #endregion

    // #region GET find books by Title containing
    @GetMapping("/searchTitle")
    @Operation(summary = "Find Books by title", description = "Retrieves a paginated and optionally sorted list of books with matching title.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved books"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters provided"),
            @ApiResponse(responseCode = "404", description = "Books not found")
    })
    public ResponseEntity<Page<Book>> searchBooksByTitle(
            @Parameter(description = "Title segment of the Book") @RequestParam("title") String title,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort fields") @RequestParam(defaultValue = "") String[] sort) {
        // Retrieve books whose title contains the search string (case-insensitive)
        Page<Book> books = repository.findByTitleContainingIgnoreCase(title, PageRequest.of(page, size, Sort.by(sort)));

        if (books.isEmpty()) {
            // If no books are found, return a 404 Not Found
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Return the list of books with a 200 OK status
        return new ResponseEntity<>(books, HttpStatus.OK);
    }
    // #endregion

    // #region GET find books by ISBN
    @GetMapping("/searchIsbn")
    @Operation(summary = "Find Books by ISBN", description = "Retrieves a Book by ISBN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved book"),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    public ResponseEntity<Book> searchBookByIsbn(
            @Parameter(description = "ISBN of the Book") @RequestParam("isbn") String isbn) {
        // Retrieve books whose title contains the search string (case-insensitive)
        Optional<Book> book = repository.findByIsbn(isbn);

        if (book.isEmpty()) {
            // If no books are found, return a 404 Not Found
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Return the list of books with a 200 OK status
        return new ResponseEntity<>(book.get(), HttpStatus.OK);
    }
    // #endregion

    // #region GET find books by any text
    @GetMapping("/search")
    @Operation(summary = "Find Books", description = "Retrieves a paginated and optionally sorted list of books with matching text.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved books"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters provided"),
            @ApiResponse(responseCode = "404", description = "Books not found")
    })
    public ResponseEntity<Page<Book>> searchBooksByText(
            @Parameter(description = "Text segment to match ISBN or Title") @RequestParam("text") String text,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort fields") @RequestParam(defaultValue = "") String[] sort) {
        // Retrieve books whose title contains the search string (case-insensitive)
        Page<Book> books = repository.findByTitleContainingIgnoreCase(text, PageRequest.of(page, size, Sort.by(sort)));

        // Get isbn search result
        Optional<Book> book = repository.findByIsbn(text);

        // Convert to list with ISBN result
        List<Book> bl = new ArrayList<Book>();
        if (!books.isEmpty()) {
            bl = books.toList();
        }
        if (book.isPresent()) {
            bl.add(book.get());
        }
        if (!bl.isEmpty()) {
            books = convertListToPage(bl, PageRequest.of(page, size, Sort.by(sort)));
        }

        if (books.isEmpty()) {
            // If no books are found, return a 404 Not Found
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Return the list of books with a 200 OK status
        return new ResponseEntity<>(books, HttpStatus.OK);
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

    // #region Helper
    public Page<Book> convertListToPage(List<Book> bookList, Pageable pageable) {
        // Calculate the start and end index based on the page size and page number
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), bookList.size());

        // Sublist to simulate the page
        List<Book> pageContent = bookList.subList(start, end);

        // Return a Page with content and pagination details
        return new PageImpl<>(pageContent, pageable, bookList.size());
    }

    // #endregion
}
