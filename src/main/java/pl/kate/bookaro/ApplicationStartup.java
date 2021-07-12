package pl.kate.bookaro;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pl.kate.bookaro.catalog.application.port.CatalogUseCase;
import pl.kate.bookaro.catalog.application.port.CatalogUseCase.CreateBookCommand;
import pl.kate.bookaro.catalog.application.port.CatalogUseCase.UpdateBookCommand;
import pl.kate.bookaro.catalog.db.AuthorJpaRepository;
import pl.kate.bookaro.catalog.domain.Author;
import pl.kate.bookaro.catalog.domain.Book;
import pl.kate.bookaro.order.application.port.PlaceOrderUseCase;
import pl.kate.bookaro.order.application.port.PlaceOrderUseCase.PlaceOrderCommand;
import pl.kate.bookaro.order.application.port.PlaceOrderUseCase.PlaceOrderResponse;
import pl.kate.bookaro.order.application.port.QueryOrderUseCase;
import pl.kate.bookaro.order.domain.OrderItem;
import pl.kate.bookaro.order.domain.Recipient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Component
@AllArgsConstructor
class ApplicationStartup implements CommandLineRunner {

    private final CatalogUseCase catalog;
    private final PlaceOrderUseCase placeOrder;
    private final QueryOrderUseCase queryOrder;
    private final AuthorJpaRepository authorJpaRepository;


    /*public ApplicationStartup(
            CatalogUseCase catalog,
            PlaceOrderUseCase placeOrder,
            QueryOrderUseCase queryOrder,
            @Value("${bookaro.catalog.query.author}") String author) {
        this.catalog = catalog;
        this.placeOrder = placeOrder;
        this.queryOrder = queryOrder;
        this.author = author;
    }
*/

    @Override
    public void run(String... args) {
        initData();
        placeOrder();


    }

    private void initData() {
        Author joshua = new Author("Joshua", "Bloch");
        Author neal = new Author("Neal", "Gafter");
        authorJpaRepository.save(joshua);
        authorJpaRepository.save(neal);

        CreateBookCommand effectiveJava = new CreateBookCommand(
                "Effective Java",
                Set.of(joshua.getId()),
                2005,
                new BigDecimal("79.00")
        );
        CreateBookCommand javaPuzzlers = new CreateBookCommand(
                "Java Puzzlers",
                Set.of(joshua.getId(), neal.getId()),
                2018,
                new BigDecimal("99.00")
        );

        catalog.addBook(effectiveJava);
        catalog.addBook(javaPuzzlers);
    }

    private void placeOrder(){
        Book effectiveJava = catalog.findOneByTitle("Effective Java").orElseThrow(() -> new IllegalStateException("Cannot find a book"));
        Book javaPuzzlers = catalog.findOneByTitle("Java Puzzlers").orElseThrow(() -> new IllegalStateException("Cannot find a book"));

        Recipient recipient = Recipient
                .builder()
                .name("Jan Kowalski")
                .phone("345-567-834")
                .street("Armi Krajowej 23")
                .city("Kraków")
                .zipCode("30-150")
                .email("jan@exampl.org")
                .build();

        PlaceOrderCommand command = PlaceOrderCommand
                .builder()
                .recipient(recipient)
                .item(new OrderItem(effectiveJava.getId(), 16))
                .item(new OrderItem(javaPuzzlers.getId(), 7))
                .build();
        PlaceOrderResponse response = placeOrder.placeOrder(command);
        /*String result = response.handle(
                orderId -> "Created ORDER with id: " + orderId,
                error -> "Failed to created order: " + error
        );
        System.out.println(result);*/
        System.out.println("Created ORDER with id: " + response.getOrderId());

        //list all orders
        queryOrder.findAll().forEach(order -> {
            System.out.println("GOT ORDER WITH TOTAL PRICE: " + order.totalPrice() + "DETAILS: " + order);
        });
    }


    /*private void findByTitle(){
        List<Book> books = catalog.findByTitle(title);
        books.forEach(System.out::println);
    }

    private void findByAuthor(){
        List<Book> booksAuthor = catalog.findByAuthor(author);
        booksAuthor.forEach(System.out::println);
    }

    private void findAndUpdate() {
        System.out.println("Updating...");
        catalog.findOneByTitleAndAuthor("Pan Tadeusz", "Mickiewicz Adam")
                .ifPresent(book -> {
                    UpdateBookCommand command = UpdateBookCommand
                            .builder()
                            .id(book.getId())
                            .title("Pan Tadeusz, czyli ostatni zajazd na litwie")
                            .build();
                    CatalogUseCase.UpdateBookResponse response =  catalog.updateBook(command);
                    System.out.println("Updating book result: " + response.isSuccess());
                });
    }*/
}
