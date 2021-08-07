package pl.kate.bookaro.order.application.price;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.kate.bookaro.catalog.domain.Book;
import pl.kate.bookaro.order.application.RichOrder;
import pl.kate.bookaro.order.domain.Order;
import pl.kate.bookaro.order.domain.OrderItem;
import pl.kate.bookaro.order.domain.OrderStatus;
import pl.kate.bookaro.order.domain.Recipient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PriceServiceTest {

    @Autowired
    PriceService priceService = new PriceService();

    @Test
    public void calculateTotalPriceOfEmptyOrder(){
        //given
        Order order = Order
                .builder()
                .build();
        //when
        OrderPrice price = priceService.calculatePrice(order);

        //then
        assertEquals(BigDecimal.ZERO, price.finalPrice());
    }

    @Test
    public void calculateTotalPrice(){
        //given
        Book book1 = new Book();
        book1.setPrice(new BigDecimal("12.50"));
        Book book2 = new Book();
        book2.setPrice(new BigDecimal("33.99"));
        Order order = Order
                .builder()
                .item(new OrderItem(book1, 2))
                .item(new OrderItem(book2, 5))
                .build();

        //when
        OrderPrice price = priceService.calculatePrice(order);
        //then
        assertEquals(new BigDecimal("194.95"), price.finalPrice());
        assertEquals(new BigDecimal("194.95"), price.getItemsPrice());
    }

}