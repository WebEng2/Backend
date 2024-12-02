package de.dhbw_ravensburg.webeng2.backend.controller;

public class LibraryException extends RuntimeException {
    private String message;

    public LibraryException(String message) {
        this.message = message;
    }
}
