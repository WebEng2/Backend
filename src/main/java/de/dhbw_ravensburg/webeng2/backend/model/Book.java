package de.dhbw_ravensburg.webeng2.backend.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

@Document("books")
public class Book {
    @Id private String id;
    private String name;
    @Indexed(unique = true) private String isbn;

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
