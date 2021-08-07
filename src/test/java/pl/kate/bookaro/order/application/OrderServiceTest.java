package pl.kate.bookaro.order.application;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import pl.kate.bookaro.catalog.application.port.CatalogUseCase;
import pl.kate.bookaro.catalog.db.BookJpaRepository;
import pl.kate.bookaro.catalog.domain.Book;
import pl.kate.bookaro.order.application.port.QueryOrderUseCase;
import pl.kate.bookaro.order.domain.Delivery;
import pl.kate.bookaro.order.domain.OrderStatus;
import pl.kate.bookaro.order.domain.Recipient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static pl.kate.bookaro.order.application.port.PlaceOrderUseCase.*;

/*@DataJpaTest
@Import({PlaceOrderService.class})*/
@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class OrderServiceTest {
    @Autowired
    BookJpaRepository bookRepository;
    @Autowired
    PlaceOrderService service;
    @Autowired
    QueryOrderUseCase queryOrderService;

    @Autowired
    CatalogUseCase catalogUseCase;

    @Test
    public void userCanPlaceOrder() {
        //given
        Book effectiveJava = givenEffectiveJava(50L);
        Book jcip = givenJavaConcurrency(50L);
        PlaceOrderCommand command = PlaceOrderCommand
                .builder()
                .recipient(recipient())
                .item(new OrderItemCommand(effectiveJava.getId(), 15))
                .item(new OrderItemCommand(jcip.getId(), 10))
                .build();
        //when
        PlaceOrderResponse response = service.placeOrder(command);
        //then
        assertTrue(response.isSuccess());
        assertEquals(35L, availableCopiesOf(effectiveJava));
        assertEquals(40L, availableCopiesOf(jcip));

    }

    @Test
    public void userCanRevokeOrder(){
        //given
        Book effectiveJava = givenEffectiveJava(50L);
        String recipient = "marek@example.org";
        Long orderId = placeOrder(effectiveJava.getId(), 15, recipient);
        assertEquals(35L, availableCopiesOf(effectiveJava));

        //when
        UpdateStatusCommand command = new UpdateStatusCommand(orderId, OrderStatus.CANCELED, recipient);
        service.updateOrderStatus(command);

        //then
        assertEquals(50L, availableCopiesOf(effectiveJava));
        //books number change back
        //order status is canceled
        assertEquals(OrderStatus.CANCELED, queryOrderService.findById(orderId).get().getStatus());


    }


    @Disabled("homework")
    public void userCannotRevokePaidOrder(){
        //user nie może wycofać już opłaconego zamówienia
        //given
        Book effectiveJava = givenEffectiveJava(50L);
        
        //when

        //then
    }

    @Disabled("homework")
    public void userCannotRevokeShippedOrder(){
        //user nie może wycofać już wysłanego zamówienia
    }

    @Disabled("homework")
    public void userCannotOrderNoExistingBooks(){
        //user nie może zamówić nie istniejących książek
    }

    @Disabled("homework")
    public void userCannotOrderNegativeNumberOfBooks(){
        //user nie może zamówić ujemnej liczby książek
    }

    @Test
    public void userCantOrderMoreBooksThanAvailable() {
        //given
        Book effectiveJava = givenEffectiveJava(5L);
        PlaceOrderCommand command = PlaceOrderCommand
                .builder()
                .recipient(recipient())
                .item(new OrderItemCommand(effectiveJava.getId(), 10))
                .build();
        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.placeOrder(command);
        });
        //then
        assertTrue(exception.getMessage().contains("Too many copies of book " + effectiveJava.getId() + " requested"));

    }

    @Test
    public void userCannotRevokeOtherUsersOrder(){
        //given
        Book effectiveJava = givenEffectiveJava(50L);
        String adam = "adam@example.org";
        Long orderId = placeOrder(effectiveJava.getId(), 15, adam);
        assertEquals(35L, availableCopiesOf(effectiveJava));
        //when
        UpdateStatusCommand command = new UpdateStatusCommand(orderId, OrderStatus.CANCELED, "marek@example.org");
        service.updateOrderStatus(command);
        //then
        assertEquals(35L, availableCopiesOf(effectiveJava));
        assertEquals(OrderStatus.NEW, queryOrderService.findById(orderId).get().getStatus());
    }

    @Test
    public void adminCannotRevokeOtherUsersOrder(){
        //given
        Book effectiveJava = givenEffectiveJava(50L);
        String marek = "marek@example.org";
        Long orderId = placeOrder(effectiveJava.getId(), 15, marek);
        assertEquals(35L, availableCopiesOf(effectiveJava));
        //when
        String admin = "admin@example.org";
        UpdateStatusCommand command = new UpdateStatusCommand(orderId, OrderStatus.CANCELED, admin);
        service.updateOrderStatus(command);
        //then
        assertEquals(50L, availableCopiesOf(effectiveJava));
        assertEquals(OrderStatus.CANCELED, queryOrderService.findById(orderId).get().getStatus());
    }

    @Test
    public void adminCanMarkOrderAsPaid(){
        //given
        Book effectiveJava = givenEffectiveJava(50L);
        String recipient = "marek@example.org";
        Long orderId = placeOrder(effectiveJava.getId(), 15, recipient);
        assertEquals(35L, availableCopiesOf(effectiveJava));

        //when
        String admin = "admin@example.org";
        UpdateStatusCommand command = new UpdateStatusCommand(orderId, OrderStatus.PAID, admin);
        service.updateOrderStatus(command);

        //then
        assertEquals(35L, availableCopiesOf(effectiveJava));
        //books number change back
        //order status is canceled
        assertEquals(OrderStatus.PAID, queryOrderService.findById(orderId).get().getStatus());

    }

    @Test
    public void shippingCostsAreAddedToTotalOrderPrice(){
        //given
        Book book = givenBook(50L, "49.90");

        //when
        Long orderId = placeOrder(book.getId(), 1);

        //then
        assertEquals("59.80", orderOf(orderId).getFinalPrice().toPlainString());

    }

    @Test
    public void shippingCostsAreDiscountedOver100zlotys(){
        //given
        Book book = givenBook(50L, "49.90");

        //when
        Long orderId = placeOrder(book.getId(), 3);

        //then
        RichOrder order = orderOf(orderId);
        assertEquals("149.70", order.getFinalPrice().toPlainString());
        assertEquals("149.70", order.getOrderPrice().getItemsPrice().toPlainString());


    }

    @Test
    public void cheapestBookIsHalfPriceWhenTotalOver200zlotys(){
        //given
        Book book = givenBook(50L, "49.90");

        //when
        Long orderId = placeOrder(book.getId(), 5);

        //then
        RichOrder order = orderOf(orderId);
        assertEquals("224.55", order.getFinalPrice().toPlainString());

    }

    @Test
    public void cheapestBookIsFreeWhenTotalOver400zlotys(){
        //given
        Book book = givenBook(50L, "49.90");

        //when
        Long orderId = placeOrder(book.getId(), 10);

        //then
        RichOrder order = orderOf(orderId);
        assertEquals("449.10", order.getFinalPrice().toPlainString());

    }



    private Book givenJavaConcurrency(long available) {
        return bookRepository.save(new Book("Java Concurrency in Practice", 2006, new BigDecimal("99.90"), available));
    }

    private Book givenEffectiveJava(long available) {
        return bookRepository.save(new Book("Effective Java", 2005, new BigDecimal("199.90"), available));
    }

    private Recipient recipient(){
        return recipient("john@examle.org");
    }

    private Recipient recipient(String email){
        return Recipient.builder().email(email).build();
    }

    private Long availableCopiesOf(Book effectiveJava) {
        return catalogUseCase.findById(effectiveJava.getId()).get().getAvailable();
    }

    private Long placeOrder(Long bookId, int copies, String recipient){
        //Book jcip = givenJavaConcurrency(50L);
        PlaceOrderCommand command = PlaceOrderCommand
                .builder()
                .recipient(recipient(recipient))
                .item(new OrderItemCommand(bookId, copies))
                .delivery(Delivery.COURIER)
                .build();
        //PlaceOrderResponse response = service.placeOrder(command);
        return service.placeOrder(command).getRight();
    }

    private Long placeOrder(Long bookId, int copies){
        return placeOrder(bookId, copies, "john@examle.org");
    }

    private Book givenBook(long available, String price){
        return bookRepository.save(new Book("Java Concurrency in Practice", 2006, new BigDecimal(price), available));
    }

    private RichOrder orderOf(Long orderId){
        return queryOrderService.findById(orderId).get();
    }
}