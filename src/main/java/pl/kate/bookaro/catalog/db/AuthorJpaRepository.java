package pl.kate.bookaro.catalog.db;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kate.bookaro.catalog.domain.Author;

public interface AuthorJpaRepository extends JpaRepository<Author, Long> {
}
