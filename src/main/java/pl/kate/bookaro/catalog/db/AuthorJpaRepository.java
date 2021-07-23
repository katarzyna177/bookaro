package pl.kate.bookaro.catalog.db;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kate.bookaro.catalog.domain.Author;

import java.util.Optional;

public interface AuthorJpaRepository extends JpaRepository<Author, Long> {

    Optional<Author> findByNameIgnoreCase(String name);
}
