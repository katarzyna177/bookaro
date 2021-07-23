package pl.kate.bookaro.catalog.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.springframework.context.event.EventListener;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pl.kate.bookaro.jpa.BaseEntity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@NoArgsConstructor
@ToString(exclude = "books")
@EntityListeners(AuditingEntityListener.class)
public class Author extends BaseEntity {

    private String name;

    @ManyToMany(mappedBy = "authors", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JsonIgnoreProperties("authors")
    private Set<Book> books = new HashSet<>();

    @CreatedDate
    private LocalDateTime createdAt;

    public Author(String name) {
        this.name = name;
    }

    public void addBook(Book book){
        books.add(book);
        book.getAuthors().add(this);
    }

    public void removeBook(Book book){
        books.remove(book);
        book.getAuthors().remove(this);
    }
}
