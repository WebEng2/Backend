package de.dhbw_ravensburg.webeng2.backend.model;

public class LibraryDTO {
    private String id;
    private String name;
    private float distance;

    public LibraryDTO(String id, String name, float distance) {
        this.id = id;
        this.name = name;
        this.distance = distance;
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

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }
}