package pl.kate.bookaro.order.web;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pl.kate.bookaro.order.application.RichOrder;
import pl.kate.bookaro.order.application.port.PlaceOrderUseCase;
import pl.kate.bookaro.order.application.port.PlaceOrderUseCase.PlaceOrderCommand;
import pl.kate.bookaro.order.application.port.PlaceOrderUseCase.PlaceOrderResponse;
import pl.kate.bookaro.order.application.port.QueryOrderUseCase;
import pl.kate.bookaro.order.domain.OrderStatus;


import java.net.URI;
import java.util.List;
import java.util.Map;

import static pl.kate.bookaro.order.application.port.PlaceOrderUseCase.*;

@RequestMapping("/orders")
@RestController
@AllArgsConstructor
public class OrderController {
    private final PlaceOrderUseCase placeOrder;
    private final QueryOrderUseCase queryOrder;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    List<RichOrder> getAll(){
        return queryOrder.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RichOrder> getById(@PathVariable Long id){
        return queryOrder
                .findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> createOrder(@RequestBody PlaceOrderCommand command) {
        return placeOrder
                .placeOrder(command)
                .handle(
                        orderId -> ResponseEntity.created(orderUri(orderId)).build(),
                        error -> ResponseEntity.badRequest().body(error)
                );
        /*PlaceOrderResponse response = placeOrder.placeOrder(command);
        if(response.isSuccess()){
            return ResponseEntity.created(orderUri(response.getOrderId())).build();
        }
        return ResponseEntity.badRequest().body(response.getErrors());*/

    }

    URI orderUri(Object orderId){
        return new CreatedURI("/" + orderId).uri();
    }

    @PutMapping("/{id}/status")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> body){
        String status = body.get("status");
        OrderStatus orderStatus = OrderStatus
                .parseString(status)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown status: " + status));
        UpdateStatusCommand command = new UpdateStatusCommand(id, orderStatus, null);
        placeOrder.updateOrderStatus(command);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    private void deleteOrder(@PathVariable Long id){
        placeOrder.removeById(id);
    }


    /*@Data
    private static class CreateOrderCommand {
        List<OrderItemCommand> items;
        RecipientCommand recipient;

        PlaceOrderCommand toPlaceOrderCommand() {
            List<OrderItem> orderItems = items
                    .stream()
                    .map(item -> new OrderItem(item.bookId, item.quantity))
                    .collect(Collectors.toList());
            return new PlaceOrderCommand(orderItems, recipient.toRecipient());
        }*/

        /*@Data
        static class OrderItemCommand {
            Long bookId;
            int quantity;
        }*/

       /* @Data
        static class RecipientCommand {
            String name;
            String phone;
            String street;
            String city;
            String zipCode;
            String email;

            Recipient toRecipient() {
                return new Recipient(name, phone, street, city, zipCode, email);
            }
        }*/

    /*}*/
}
