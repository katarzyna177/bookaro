package pl.kate.bookaro.catalog.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@RequiredArgsConstructor
@ToString(exclude = "authors")
@Entity
public class Book {
    @Id
    @GeneratedValue
    private Long id;
    private String title;
    //private String author;
    private Integer year;
    private BigDecimal price;
    private Long coverId;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable
    @JsonIgnoreProperties("books")
    private Set<Author> authors;

    public Book(String title, Integer year, BigDecimal price) {
        this.title = title;
        //this.author = author;
        this.year = year;
        this.price = price;
    }
}
