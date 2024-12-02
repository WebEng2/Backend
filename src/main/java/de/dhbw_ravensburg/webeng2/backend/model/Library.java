package de.dhbw_ravensburg.webeng2.backend.model;

import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

import org.springframework.data.annotation.Id;

@Document(collection = "libraries")
public class Library {
    @Id
    private String id;

    @NotNull(message = "Name cannot be null")
    @NotEmpty(message = "Name cannot be empty")
    private String name;

    @NotNull(message = "Inventory cannot be null")
    private List<String> isbnList;

    // Constructors
    public Library(String name, List<String> isbnList) {
        this.name = name;
        this.isbnList = isbnList;
    }

    public Library() {
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getIsbnList() {
        return isbnList;
    }

    public void setIsbnList(List<String> isbnList) {
        this.isbnList = isbnList;
    }

    @Override
    public String toString() {
        return "Library{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", isbnList=" + isbnList +
                '}';
    }
}
