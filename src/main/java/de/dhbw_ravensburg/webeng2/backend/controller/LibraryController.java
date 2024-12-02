package de.dhbw_ravensburg.webeng2.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import de.dhbw_ravensburg.webeng2.backend.model.Library;
import de.dhbw_ravensburg.webeng2.backend.model.LibraryDTO;
import de.dhbw_ravensburg.webeng2.backend.repos.LibraryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/libraries")
public class LibraryController {

    @Autowired
    private LibraryRepository repository;

    // #region GET all Libraries
    @GetMapping("/")
    @Operation(summary = "Get all Libraries", description = "Retrieves a paginated and optionally sorted list of libraries.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved libraries"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters provided"),
            @ApiResponse(responseCode = "404", description = "No libraries in Database")
    })
    public ResponseEntity<Page<LibraryDTO>> findLibraries(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort fields") @RequestParam(defaultValue = "") String[] sort) {
        Page<Library> libraries = repository.findAll(PageRequest.of(page, size, Sort.by(sort)));
        if (libraries.isEmpty()) {
            // If no libraries are found, return a 404 Not Found
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Return the list of libraries with a 200 OK status
        Page<LibraryDTO> libs = convertListDTOToPage(libraries.stream()
                .map(library -> new LibraryDTO(library.getId(), library.getName()))
                .collect(Collectors.toList()), PageRequest.of(page, size, Sort.by(sort)));
        return new ResponseEntity<>(libs, HttpStatus.OK);
    }
    // #endregion

    // #region POST new Library
    @PostMapping("/")
    @Operation(summary = "Create new Library", description = "Creates a new library.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created a new library"),
            @ApiResponse(responseCode = "400", description = "Invalid library data provided"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Library> postLibrary(
            @Parameter(description = "The library to be added to the repository") @Valid @RequestBody Library library) {
        // Save the new library to MongoDB
        Library b = library;
        if (b == null) {
            throw new LibraryException("Can't convert input to Library");
        }
        Library savedLibrary = repository.save(b);
        // Return the saved library with a 201 status code
        return new ResponseEntity<>(savedLibrary, HttpStatus.CREATED);
    }
    // #endregion

    // #region GET Library by ID
    @GetMapping("/{id}")
    @Operation(summary = "Get Library by ID", description = "Retrieves a library")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved library"),
            @ApiResponse(responseCode = "400", description = "Invalid library data provided"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Library findById(
            @Parameter(description = "The internal id of the library") @RequestParam @PathVariable String id) {
        return repository.findById(id).orElseThrow(() -> new LibraryException("Unknown ID"));
    }
    // #endregion

    // #region PUT Update Library by ID
    @PutMapping("/{id}")
    @Operation(summary = "Update a Library", description = "Updates an existing library.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated library"),
            @ApiResponse(responseCode = "400", description = "Invalid library data provided"),
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "Library does not exist")
    })
    public ResponseEntity<Library> updateLibrary(
            @Parameter(description = "The id of the library to edid") @PathVariable("id") String id,
            @Parameter(description = "The library to be added to the repository") @Valid @RequestBody Library library) {
        // Check if the library exists in the database
        Optional<Library> existingLibraryOptional = repository.findById(id);

        if (!existingLibraryOptional.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Return 404 if the library doesn't exist
        }
        Library b = library;
        if (b == null) {
            throw new LibraryException("Can't convert input to Library");
        }

        // Get the existing library
        Library existingLibrary = existingLibraryOptional.get();

        // Update the fields of the existing library
        existingLibrary.setName(b.getName());
        existingLibrary.setIsbnList(b.getIsbnList());

        // Save the updated library
        Library updatedLibrary = repository.save(existingLibrary);

        // Return the updated library with a 200 OK status code
        return new ResponseEntity<>(updatedLibrary, HttpStatus.OK);
    }
    // #endregion

    // #region GET find libraries by Name containing
    @GetMapping("/searchName")
    @Operation(summary = "Find Libraries by name", description = "Retrieves a paginated and optionally sorted list of libraries with matching name.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved libraries"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters provided"),
            @ApiResponse(responseCode = "404", description = "Libraries not found")
    })
    public ResponseEntity<Page<LibraryDTO>> searchLibrariesByTitle(
            @Parameter(description = "Name segment of the Library") @RequestParam("name") String name,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort fields") @RequestParam(defaultValue = "") String[] sort) {
        // Retrieve libraries whose name contains the search string (case-insensitive)
        Page<Library> libraries = repository.findByNameContainingIgnoreCase(name,
                PageRequest.of(page, size, Sort.by(sort)));

        if (libraries.isEmpty()) {
            // If no libraries are found, return a 404 Not Found
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Return the list of libraries with a 200 OK status
        Page<LibraryDTO> libs = convertListDTOToPage(libraries.stream()
                .map(library -> new LibraryDTO(library.getId(), library.getName()))
                .collect(Collectors.toList()), PageRequest.of(page, size, Sort.by(sort)));
        return new ResponseEntity<>(libs, HttpStatus.OK);
    }
    // #endregion

    // #region GET find libraries by ISBN in Stock
    @GetMapping("/searchHasISBN")
    @Operation(summary = "Find Libraries stocking a Book with ISBN", description = "Retrieves a paginated and optionally sorted list of libraries that stock the book.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved libraries"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters provided"),
            @ApiResponse(responseCode = "404", description = "Libraries not found")
    })
    public ResponseEntity<Page<LibraryDTO>> searchLibrariesHasISBN(
            @Parameter(description = "ISBN of desired Book") @RequestParam("isbn") String isbn,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort fields") @RequestParam(defaultValue = "") String[] sort) {
        // Retrieve libraries whose name contains the search string (case-insensitive)
        Page<Library> libraries = repository.findByIsbnListContaining(
                isbn,
                PageRequest.of(page, size, Sort.by(sort)));

        if (libraries.isEmpty()) {
            // If no libraries are found, return a 404 Not Found
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Return the list of libraries with a 200 OK status
        Page<LibraryDTO> libs = convertListDTOToPage(libraries.stream()
                .map(library -> new LibraryDTO(library.getId(), library.getName()))
                .collect(Collectors.toList()), PageRequest.of(page, size, Sort.by(sort)));
        return new ResponseEntity<>(libs, HttpStatus.OK);
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

    @ExceptionHandler(LibraryException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> onLibraryException(LibraryException ex) {
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
    public Page<Library> convertListToPage(List<Library> libraryList, Pageable pageable) {
        // Calculate the start and end index based on the page size and page number
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), libraryList.size());

        // Sublist to simulate the page
        List<Library> pageContent = libraryList.subList(start, end);

        // Return a Page with content and pagination details
        return new PageImpl<>(pageContent, pageable, libraryList.size());
    }

    public Page<LibraryDTO> convertListDTOToPage(List<LibraryDTO> libraryList, Pageable pageable) {
        // Calculate the start and end index based on the page size and page number
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), libraryList.size());

        // Sublist to simulate the page
        List<LibraryDTO> pageContent = libraryList.subList(start, end);

        // Return a Page with content and pagination details
        return new PageImpl<>(pageContent, pageable, libraryList.size());
    }

    // #endregion

}
