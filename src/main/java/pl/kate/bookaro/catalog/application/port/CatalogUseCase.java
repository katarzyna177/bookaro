package pl.kate.bookaro.catalog.application.port;

import lombok.*;
import pl.kate.bookaro.catalog.domain.Book;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptyList;

public interface CatalogUseCase {
    List<Book> findByTitle(String title);

    Optional<Book> findOneByTitle(String title);

    Optional<Book> findById(Long id);

    Optional<Book> findOneByAuthor(String author);

    List<Book> findByAuthor(String author);

    List<Book> findAll();

    List<Book> findByTitleAndAuthor(String title, String author);

    Book addBook(CreateBookCommand command);

    void removeById(Long id);

    UpdateBookResponse updateBook(UpdateBookCommand command);

    void UpdateBookCover(UpdateBookCoverCommand command);

    void removeBookCover(Long id);


    @Value
    class UpdateBookCoverCommand{
        Long id;
        byte[] file;
        String contentType;
        String filename;
    }


    @Value
    class CreateBookCommand {
        String title;
        Set<Long> authors;
        Integer year;
        BigDecimal price;

    }

    @Value
    @Builder
    @AllArgsConstructor
    class UpdateBookCommand{
        Long id;
        String title;
        Set<Long> authors ;
        Integer year;
        BigDecimal price;


    }

    @Value
class UpdateBookResponse{
        public static UpdateBookResponse SUCCESS = new UpdateBookResponse(true, emptyList());
        boolean success;
        List<String> errors;
    }
}
