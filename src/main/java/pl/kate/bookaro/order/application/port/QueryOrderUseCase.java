package pl.kate.bookaro.order.application.port;

import lombok.Value;
import pl.kate.bookaro.catalog.domain.Book;
import pl.kate.bookaro.order.domain.OrderItem;
import pl.kate.bookaro.order.domain.OrderStatus;
import pl.kate.bookaro.order.domain.Recipient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static pl.kate.bookaro.order.application.port.PlaceOrderUseCase.*;

public interface QueryOrderUseCase {
    List<RichOrder> findAll();

    Optional<RichOrder> findById(Long id);

    @Value
    class RichOrder {
        Long id;
        OrderStatus status;
        Set<OrderItem> items;
        Recipient recipient;
        LocalDateTime createdAt;

        public BigDecimal totalPrice() {
            return items.stream()
                    .map(item -> item.getBook().getPrice().multiply(new BigDecimal(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    /*@Value
    class RichOrderItem {
        Book book;
        int quantity;
    }*/
}
