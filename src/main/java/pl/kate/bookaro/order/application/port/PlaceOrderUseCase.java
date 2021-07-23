package pl.kate.bookaro.order.application.port;

import lombok.*;
import pl.kate.bookaro.catalog.domain.Book;
import pl.kate.bookaro.order.domain.Order;
import pl.kate.bookaro.order.domain.OrderItem;
import pl.kate.bookaro.order.domain.OrderStatus;
import pl.kate.bookaro.order.domain.Recipient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface PlaceOrderUseCase {
    PlaceOrderResponse placeOrder(PlaceOrderCommand command);

    void updateOrderStatus(Long id, OrderStatus status);

    void removeById(Long id);

    @Builder
    @Value
    @AllArgsConstructor
    class PlaceOrderCommand{
        @Singular
        List<OrderItemCommand> items;
        Recipient recipient;
    }

    @Value
    class OrderItemCommand {
        Long bookId;
        int quantity;
    }

    @Value
    class PlaceOrderResponse{
        boolean success;
        Long orderId;
        List<String> errors;


        public static PlaceOrderResponse success(Long orderId){
            return new PlaceOrderResponse(true, orderId, Collections.emptyList());
        }

        public static PlaceOrderResponse failure(String... errors){
            return new PlaceOrderResponse(false, null, Arrays.asList(errors));
        }

    }

}
