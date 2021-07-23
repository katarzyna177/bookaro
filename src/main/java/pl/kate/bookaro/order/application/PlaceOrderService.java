package pl.kate.bookaro.order.application;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import pl.kate.bookaro.catalog.db.BookJpaRepository;
import pl.kate.bookaro.catalog.domain.Book;
import pl.kate.bookaro.order.application.port.PlaceOrderUseCase;
import pl.kate.bookaro.order.db.OrderJpaRepository;
import pl.kate.bookaro.order.db.RecipientJpaRepository;
import pl.kate.bookaro.order.domain.*;

import javax.transaction.Transactional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PlaceOrderService implements PlaceOrderUseCase {
    private final OrderJpaRepository repository;
    private final BookJpaRepository bookJpaRepository;
    private final RecipientJpaRepository recipientJpaRepository;

    @Override
    public PlaceOrderResponse placeOrder(PlaceOrderCommand command) {
        Set<OrderItem> items = command.getItems()
                .stream()
                .map(this::toOrderItem)
                .collect(Collectors.toSet());
        Order order = Order
                .builder()
                .recipient(getOrCreateRecipient(command.getRecipient()))
                .items(items)
                .build();
        Order save = repository.save(order);
        bookJpaRepository.saveAll(reduceBooks(items));
        return PlaceOrderResponse.success(save.getId());
    }

    private Recipient getOrCreateRecipient(Recipient recipient) {
        return recipientJpaRepository
                .findByEmailIgnoreCase(recipient.getEmail())
                .orElse(recipient);

    }

    private Recipient getRecipient(PlaceOrderCommand command) {
        return command.getRecipient();
    }

    private Set<Book> reduceBooks(Set<OrderItem> items) {
        return items.stream()
                .map(item -> {
                    Book book = item.getBook();
                    book.setAvailable(book.getAvailable() - item.getQuantity());
                    return book;
                }).collect(Collectors.toSet());
    }

    private OrderItem toOrderItem(OrderItemCommand command) {
        Book book = bookJpaRepository.getOne(command.getBookId());
        int quantity = command.getQuantity();
        if(book.getAvailable() >= quantity){
            return new OrderItem(book, quantity);
        }
        throw new IllegalArgumentException("Too many copies of book " + book.getId() + " requested " + quantity + " of " + book.getAvailable() + " available ");
    }

    @Override
    public void updateOrderStatus(Long id, OrderStatus status) {
        repository.findById(id)
                .ifPresent(order -> {
                    //order.setStatus(status);
                    UpdateStatusResult result = order.updateStatus(status);
                    if(result.isRevoked()){
                        bookJpaRepository.saveAll(revokeBooks(order.getItems()));
                    }

                    repository.save(order);
        });
    }

    private Set<Book> revokeBooks(Set<OrderItem> items) {
        return items.stream()
                .map(item -> {
                    Book book = item.getBook();
                    book.setAvailable(book.getAvailable() + item.getQuantity());
                    return book;
                }).collect(Collectors.toSet());
    }

    @Override
    public void removeById(Long id) {
        repository.deleteById(id);
    }

}
