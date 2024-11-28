package de.dhbw_ravensburg.webeng2.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.lang.NonNull;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import de.dhbw_ravensburg.webeng2.backend.model.BookInfo;
import de.dhbw_ravensburg.webeng2.backend.repos.RedisRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class BookInfoService {
    private static final String GOOGLE_BOOKS_API = "https://www.googleapis.com/books/v1/volumes?q=isbn:";
    private static final String OPEN_LIBRARY_API = "https://openlibrary.org/api/books?bibkeys=ISBN:{isbn}&format=json&jscmd=data";
    
    @Autowired
    private RedisRepository redisRepository;
    
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Retrieves book information from cache or external APIs.
     * First checks Redis cache, if not found fetches from Google Books and Open Library APIs.
     *
     * @param isbn The ISBN of the book to lookup
     * @return BookInfo object containing consolidated information from various sources
     */
    @Operation(summary = "Get Book Information", 
              description = "Retrieves book information from cache or external APIs (Google Books and Open Library)")
    public BookInfo getBookInfo(@Parameter(description = "ISBN of the book") @NonNull String isbn) {
        return redisRepository.findById(isbn)
            .orElseGet(() -> fetchAndCacheBookInfo(isbn));
    }

    /**
     * Fetches book information from external APIs and caches the result.
     * Combines data from Google Books API and Open Library API.
     *
     * @param isbn The ISBN of the book to fetch information for
     * @return BookInfo object containing the fetched and cached information
     */
    private BookInfo fetchAndCacheBookInfo(@NonNull String isbn) {
        var bookInfo = new BookInfo(isbn);
        
        // Fetch from Google Books
        var googleResponse = restTemplate.getForObject(GOOGLE_BOOKS_API + isbn, GoogleBooksResponse.class);
        if (googleResponse != null && googleResponse.items != null && !googleResponse.items.isEmpty()) {
            updateFromGoogleBooks(bookInfo, googleResponse.items.get(0).volumeInfo);
        }
        
        var openLibraryUrl = OPEN_LIBRARY_API.replace("{isbn}", isbn);
        var openLibraryResponse = restTemplate.getForObject(openLibraryUrl, Map.class);
        if (openLibraryResponse != null && !openLibraryResponse.isEmpty() && openLibraryResponse.get("ISBN:" + isbn) instanceof Map) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> bookData = (Map<String, Object>) openLibraryResponse.get("ISBN:" + isbn);
                updateFromOpenLibrary(bookInfo, bookData);
            } catch (ClassCastException e) {
                // ...
            }
        }
        
        return bookInfo.getTitle() != null ? redisRepository.save(bookInfo) : null;
    }

    /**
     * Updates BookInfo object with data from Google Books API response.
     *
     * @param bookInfo The BookInfo object to update
     * @param volumeInfo The Google Books API volume information
     */
    private void updateFromGoogleBooks(BookInfo bookInfo, VolumeInfo volumeInfo) {
        bookInfo.setTitle(volumeInfo.title);
        bookInfo.setDescription(volumeInfo.description);
        bookInfo.setAuthors(String.join(", ", volumeInfo.authors));
        
        bookInfo.setPublishedDate(volumeInfo.publishedDate);
        bookInfo.setPageCount(volumeInfo.pageCount);
        bookInfo.setLanguage(volumeInfo.language);
        bookInfo.setPrintType(volumeInfo.printType);
        bookInfo.setCategories(volumeInfo.categories != null ? String.join(", ", volumeInfo.categories) : null);
        bookInfo.setAverageRating(volumeInfo.averageRating);
        bookInfo.setRatingsCount(volumeInfo.ratingsCount);
        bookInfo.setPreviewLink(volumeInfo.previewLink);
        bookInfo.setInfoLink(volumeInfo.infoLink);
    }

    /**
     * Updates BookInfo object with data from Open Library API response.
     *
     * @param bookInfo The BookInfo object to update
     * @param olData The Open Library API response data
     */
    private void updateFromOpenLibrary(BookInfo bookInfo, Map<String, Object> olData) {
        if (olData == null) return;
        
        if (bookInfo.getTitle() == null && olData.get("title") instanceof String) {
            bookInfo.setTitle((String) olData.get("title"));
        }
        
        if (bookInfo.getAuthors() == null && olData.get("authors") instanceof List) {
            try {
                @SuppressWarnings("unchecked")
                List<Map<String, String>> authors = (List<Map<String, String>>) olData.get("authors");
                bookInfo.setAuthors(authors.stream()
                    .map(author -> author.get("name"))
                    .filter(name -> name != null)
                    .collect(Collectors.joining(", ")));
            } catch (ClassCastException e) {
                // ...
            }
        }
        
        if (olData.get("weight") instanceof String) {
            bookInfo.setWeight((String) olData.get("weight"));
        }
        
        if (olData.get("subjects") instanceof List) {
            try {
                @SuppressWarnings("unchecked")
                List<Map<String, String>> subjects = (List<Map<String, String>>) olData.get("subjects");
                bookInfo.setSubjects(subjects.stream()
                    .map(subject -> subject.get("name"))
                    .filter(name -> name != null)
                    .collect(Collectors.toList()));
            } catch (ClassCastException e) {
                // ...
            }
        }
        
        if (olData.get("publishers") instanceof List) {
            try {
                @SuppressWarnings("unchecked")
                List<Map<String, String>> publishers = (List<Map<String, String>>) olData.get("publishers");
                if (!publishers.isEmpty() && publishers.get(0) != null) {
                    bookInfo.setPublisherName(publishers.get(0).get("name"));
                }
            } catch (ClassCastException e) {
                // ...
            }
        }
        
        if (olData.get("excerpts") instanceof List) {
            try {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> excerpts = (List<Map<String, Object>>) olData.get("excerpts");
                excerpts.stream()
                    .filter(excerpt -> excerpt != null && Boolean.TRUE.equals(excerpt.get("first_sentence")))
                    .findFirst()
                    .ifPresent(excerpt -> {
                        if (excerpt.get("text") instanceof String) {
                            bookInfo.setFirstSentence((String) excerpt.get("text"));
                        }
                    });
            } catch (ClassCastException e) {
                // ...
            }
        }
        
        if (olData.get("identifiers") instanceof Map) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, List<String>> identifiers = (Map<String, List<String>>) olData.get("identifiers");
                bookInfo.setIdentifiers(identifiers);
            } catch (ClassCastException e) {
                // ...
            }
        }
        
        if (olData.get("cover") instanceof Map) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, String> cover = (Map<String, String>) olData.get("cover");
                bookInfo.setCoverUrls(cover);
            } catch (ClassCastException e) {
                // ...
            }
        }
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
        public String publishedDate;
        public int pageCount;
        public String language;
        public String printType;
        public java.util.List<String> categories;
        public double averageRating;
        public int ratingsCount;
        public String previewLink;
        public String infoLink;
    }
    
}