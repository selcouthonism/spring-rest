package org.brokage.stockorders.domain.model.customer;


import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class Customer {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Instant createDate;

    public Customer() {}
    public Customer(Long id) {
        this.id = id;
    }




}
