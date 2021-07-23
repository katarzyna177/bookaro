package pl.kate.bookaro.catalog.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kate.bookaro.catalog.domain.Book;

import java.util.List;
import java.util.Optional;

public interface BookJpaRepository extends JpaRepository<Book, Long> {

    //List<Book> findByAuthors_nameContainsIgnoreCaseOrAuthors(String name);
    @Query("SELECT DISTINCT b FROM Book b JOIN FETCH b.authors")
    List<Book> findAllEager();

    List<Book> findByTitleStartingWithIgnoreCase(String title);

    Optional<Book> findDistinctFirstByTitleContainsIgnoreCase(String title);

    Optional<Book> findDistinctFirstByAuthorsContainsIgnoreCase(String author);

    //List<Book> findByAuthor(String name);

    //List<Book> findByAuthor(String name);

    @Query(
            "SELECT b FROM Book b JOIN b.authors a" +
                    " WHERE " +
                    " lower(a.name)  LIKE lower(concat('%', :name,'%'))")
    List<Book> findByAuthor(@Param("name") String name);

    @Query(
            "SELECT b FROM Book b JOIN b.authors a" +
                    " WHERE " +
                    " lower(b.title) LIKE lower(concat('%', :title,'%'))" +
                    " OR lower(a.name) LIKE lower(concat('%', :name,'%')) ")
    List<Book> findByTitleAndAuthor(@Param("title") String title, @Param("name") String name);
}
