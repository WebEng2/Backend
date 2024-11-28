package de.dhbw_ravensburg.webeng2.backend.controller;

public class BookException extends RuntimeException {
    private String message;

    public BookException(String message) {
        this.message = message;
    }
}
