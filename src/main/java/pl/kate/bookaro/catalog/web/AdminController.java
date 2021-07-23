package pl.kate.bookaro.catalog.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.kate.bookaro.catalog.application.port.CatalogInitializerUseCase;
import pl.kate.bookaro.catalog.application.port.CatalogUseCase;
import pl.kate.bookaro.catalog.db.AuthorJpaRepository;
import pl.kate.bookaro.catalog.domain.Author;
import pl.kate.bookaro.catalog.domain.Book;
import pl.kate.bookaro.order.application.port.PlaceOrderUseCase;
import pl.kate.bookaro.order.application.port.QueryOrderUseCase;
import pl.kate.bookaro.order.domain.OrderItem;
import pl.kate.bookaro.order.domain.Recipient;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Set;

import static pl.kate.bookaro.order.application.port.PlaceOrderUseCase.*;

@Slf4j
@RestController
@RequestMapping("/admin")
@AllArgsConstructor
public class AdminController {

    private final CatalogInitializerUseCase initializer;


    @PostMapping("/initialization")
    @Transactional
    public void initialize() {
        initializer.initialize();
    }


}
