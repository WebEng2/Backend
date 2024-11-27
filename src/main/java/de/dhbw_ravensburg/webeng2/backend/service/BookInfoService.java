package de.dhbw_ravensburg.webeng2.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import de.dhbw_ravensburg.webeng2.backend.model.Redis;
import de.dhbw_ravensburg.webeng2.backend.repos.RedisRepository;

@Service
public class BookInfoService {
    private static final String GOOGLE_BOOKS_API = "https://www.googleapis.com/books/v1/volumes?q=isbn:";
    
    @Autowired
    private RedisRepository redisRepository;
    
    private final RestTemplate restTemplate = new RestTemplate();

    public Redis getBookInfo(String isbn) {
        return redisRepository.findById(isbn)
            .orElseGet(() -> fetchAndCacheBookInfo(isbn));
    }

    private Redis fetchAndCacheBookInfo(String isbn) {
        var response = restTemplate.getForObject(GOOGLE_BOOKS_API + isbn, GoogleBooksResponse.class);
        
        if (response != null && response.items != null && !response.items.isEmpty()) {
            var volumeInfo = response.items.get(0).volumeInfo;
            var bookInfo = new Redis(isbn);
            bookInfo.setTitle(volumeInfo.title);
            bookInfo.setDescription(volumeInfo.description);
            bookInfo.setAuthors(String.join(", ", volumeInfo.authors));
            bookInfo.setThumbnail(volumeInfo.imageLinks != null ? volumeInfo.imageLinks.thumbnail : null);
            
            return redisRepository.save(bookInfo);
        }
        
        return null;
    }

    private static class GoogleBooksResponse {
        public java.util.List<Item> items;
    }
    
    private static class Item {
        public VolumeInfo volumeInfo;
    }
    
    private static class VolumeInfo {
        public String title;
        public String description;
        public java.util.List<String> authors;
        public ImageLinks imageLinks;
    }
    
    private static class ImageLinks {
        public String thumbnail;
    }
}