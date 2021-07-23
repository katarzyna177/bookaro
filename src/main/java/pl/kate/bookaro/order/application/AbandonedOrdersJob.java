package pl.kate.bookaro.order.application;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.kate.bookaro.order.application.port.PlaceOrderUseCase;
import pl.kate.bookaro.order.db.OrderJpaRepository;
import pl.kate.bookaro.order.domain.Order;
import pl.kate.bookaro.order.domain.OrderStatus;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class AbandonedOrdersJob {
    private final OrderJpaRepository repository;
    private final PlaceOrderUseCase orderUseCase;
    private final OrderProperties properties;

    @Transactional
    @Scheduled(cron = "${app.orders.abandon-cron}")
    public void run(){
        Duration paymentPeriod = properties.getPaymentPeriod();
        LocalDateTime olderThan = LocalDateTime.now().minus(paymentPeriod);
        List<Order> orders = repository.findByStatusAndCreatedAtLessThanEqual(OrderStatus.NEW, olderThan);
        log.info("Found orders to be abandoned: " + orders.size());
        orders.forEach(order -> orderUseCase.updateOrderStatus(order.getId(), OrderStatus.ABANDONED));
    }
}
