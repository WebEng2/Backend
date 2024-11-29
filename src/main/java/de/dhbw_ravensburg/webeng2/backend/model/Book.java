package de.dhbw_ravensburg.webeng2.backend.model;

import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

@Document(collection = "books")
public class Book {
    @Id
    private String id;

    @NotNull(message = "Title cannot be null")
    @NotEmpty(message = "Title cannot be empty")
    private String title;

    @NotNull(message = "ISBN cannot be null")
    @Indexed(unique = true)
    @Size(min = 10, max = 13, message = "ISBN should be between 10 and 13 characters")
    @Pattern(regexp = "^([0-9]{3})?[0-9]{10}$")
    private String isbn;

    // Constructors
    public Book(String isbn, String title) {
        this.isbn = isbn;
        this.title = title;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    @Override
    public String toString() {
        return String.format(
                "Book[isbn=%s, title='%s']",
                isbn, title);
    }
}
