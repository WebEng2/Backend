package de.dhbw_ravensburg.webeng2.backend.model;

import org.springframework.context.annotation.Document;
import org.springframework.context.annotation.Id;

@Document("Book")
public class Book {
    @Id
    private int id;

    public Book(int id) {
        super();
        this.id = id;
    }
}
