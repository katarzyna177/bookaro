package pl.kate.bookaro.order.application;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import pl.kate.bookaro.order.application.port.PlaceOrderUseCase;
import pl.kate.bookaro.order.db.OrderJpaRepository;
import pl.kate.bookaro.order.domain.Order;
import pl.kate.bookaro.order.domain.OrderStatus;

@Service
@RequiredArgsConstructor
public class PlaceOrderService implements PlaceOrderUseCase {
    private final OrderJpaRepository repository;

    @Override
    public PlaceOrderResponse placeOrder(PlaceOrderCommand command) {
        Order order = Order
                .builder()
                .recipient(command.getRecipient())
                .items(command.getItems())
                .build();
        Order save = repository.save(order);
        return PlaceOrderResponse.success(save.getId());
    }

    @Override
    public void updateOrderStatus(Long id, OrderStatus status) {
        repository.findById(id)
                .ifPresent(order -> {
                    order.setStatus(status);
                    repository.save(order);
        });
    }

    @Override
    public void removeById(Long id) {
        repository.deleteById(id);
    }

}
