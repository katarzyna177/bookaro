package pl.kate.bookaro.order.domain;

import lombok.*;
import pl.kate.bookaro.jpa.BaseEntity;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Recipient extends BaseEntity {
    private String name;
    private String phone;
    private String street;
    private String city;
    private String zipCode;
    private String email;

}
