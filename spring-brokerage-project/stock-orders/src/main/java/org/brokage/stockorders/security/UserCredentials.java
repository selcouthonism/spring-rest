package org.brokage.stockorders.security;

import jakarta.persistence.*;
import lombok.*;
import org.brokage.stockorders.model.entity.Customer;
import org.brokage.stockorders.model.enums.Role;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "user_credentials",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"username"})
        },
        indexes = {
                @Index(name = "idx_user_credentials_customer_date", columnList = "customer_id, create_date DESC"),
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCredentials {

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role; // ADMIN or CUSTOMER

    @CreationTimestamp
    @Column(name = "create_date", nullable = false, updatable = false)
    private Instant createDate;

    @Column(nullable = false)
    private boolean active = true;


    private UserCredentials(Customer customer, String username, String password, Role role, boolean active) {
        this.customer = customer;
        this.username = username;
        this.passwordHash = password;
        this.role = role;
        this.active = active;
    }

    public static UserCredentials of(Customer customer, String username, String password, Role role, boolean active) {
        return new UserCredentials(customer, username, password, role, active);
    }
}
