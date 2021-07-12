package pl.kate.bookaro.order.db;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kate.bookaro.order.domain.Order;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {
}
