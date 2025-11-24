package org.brokage.stockorders.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.brokage.stockorders.domain.model.Role;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long customerId;

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


    private UserCredentials(Long customerId, String username, String password, Role role, boolean active) {
        this.customerId = customerId;
        this.username = username;
        this.passwordHash = password;
        this.role = role;
        this.active = active;
    }

    public static UserCredentials of(Long customerId, String username, String password, Role role, boolean active) {
        return new UserCredentials(customerId, username, password, role, active);
    }

    @Override
    public String toString() {
        return "UserCredentials{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", username='" + username + '\'' +
                ", passwordHash='" + passwordHash + '\'' +
                ", role=" + role +
                ", createDate=" + createDate +
                ", active=" + active +
                '}';
    }
}
