package pl.kate.bookaro.order.domain;

import lombok.*;
import pl.kate.bookaro.catalog.domain.Book;
import pl.kate.bookaro.jpa.BaseEntity;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class OrderItem extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;
    private int quantity;

}
