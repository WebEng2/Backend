package de.dhbw_ravensburg.webeng2.backend.controller;

import de.dhbw_ravensburg.webeng2.backend.model.User;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/example")
public class BackendApplicationControllerExample {
    @GetMapping(path="/hello")
    public String sayHello() {
        return "Hello World";
    }

    @PostMapping(path="/hello")
    public String postHello() {
        return "Hello POST World";
    }

    @GetMapping(path="/user")
    public User getUser() {
        return new User("uname", "12345");
    }
}
