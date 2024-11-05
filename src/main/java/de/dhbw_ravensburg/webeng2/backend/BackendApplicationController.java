package de.dhbw_ravensburg.webeng2.backend;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
public class BackendApplicationController {
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
