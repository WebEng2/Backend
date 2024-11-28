package de.dhbw_ravensburg.webeng2.backend.model;

import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

@Document("books")
public class Book {
    @Id
    private String id;

    @NotEmpty
    private String name;

    @NotNull
    @Indexed(unique = true)
    @Size(max = 13)
    @Pattern(regexp = "^([0-9]{3})?[0-9]{10}$")
    private String isbn;

    public Book(String isbn, String name) {
        super();
        this.isbn = isbn;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
                "Book[isbn=%s, name='%s']",
                isbn, name);
    }
}
