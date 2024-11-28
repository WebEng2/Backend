package de.dhbw_ravensburg.webeng2.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.List;
import java.util.Map;

@RedisHash("BookInfo")
public class BookInfo {
    @Id
    private String isbn;
    private String title;
    private String description;
    private String authors;
    private String publishedDate;
    private int pageCount;
    private String language;
    private String printType;
    private String categories;
    private double averageRating;
    private int ratingsCount;
    private String previewLink;
    private String infoLink;
    private String weight;
    private List<String> subjects;
    private String publisherName;
    private String firstSentence;
    private Map<String, List<String>> identifiers;
    private Map<String, String> coverUrls;

    public BookInfo(String isbn) {
        this.isbn = isbn;
    }

    // Getters and Setters
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAuthors() { return authors; }
    public void setAuthors(String authors) { this.authors = authors; }

    public String getPublishedDate() { return publishedDate; }
    public void setPublishedDate(String publishedDate) { this.publishedDate = publishedDate; }
    
    public int getPageCount() { return pageCount; }
    public void setPageCount(int pageCount) { this.pageCount = pageCount; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public String getPrintType() { return printType; }
    public void setPrintType(String printType) { this.printType = printType; }
    
    public String getCategories() { return categories; }
    public void setCategories(String categories) { this.categories = categories; }
    
    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
    
    public int getRatingsCount() { return ratingsCount; }
    public void setRatingsCount(int ratingsCount) { this.ratingsCount = ratingsCount; }
    
    public String getPreviewLink() { return previewLink; }
    public void setPreviewLink(String previewLink) { this.previewLink = previewLink; }
    
    public String getInfoLink() { return infoLink; }
    public void setInfoLink(String infoLink) { this.infoLink = infoLink; }
    
    public String getWeight() { return weight; }
    public void setWeight(String weight) { this.weight = weight; }
    
    public List<String> getSubjects() { return subjects; }
    public void setSubjects(List<String> subjects) { this.subjects = subjects; }
    
    public String getPublisherName() { return publisherName; }
    public void setPublisherName(String publisherName) { this.publisherName = publisherName; }
    
    public String getFirstSentence() { return firstSentence; }
    public void setFirstSentence(String firstSentence) { this.firstSentence = firstSentence; }
    
    public Map<String, List<String>> getIdentifiers() { return identifiers; }
    public void setIdentifiers(Map<String, List<String>> identifiers) { this.identifiers = identifiers; }
    
    public Map<String, String> getCoverUrls() { return coverUrls; }
    public void setCoverUrls(Map<String, String> coverUrls) { this.coverUrls = coverUrls; }
}