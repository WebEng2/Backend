package de.dhbw_ravensburg.webeng2.backend;

public class User {
    private String username;
    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User() {}
    public String  getUsername() {
        return username;
    }
    public String  getPassword() {
        return password;
    }
}
