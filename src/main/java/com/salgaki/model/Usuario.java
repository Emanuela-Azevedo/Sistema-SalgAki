package com.salgaki.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "usuario", uniqueConstraints = {
        @UniqueConstraint(name = "uk_usuario_username", columnNames = "username"),
        @UniqueConstraint(name = "uk_usuario_singleton", columnNames = "singleton_key")
}
)
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "singleton_key", nullable = false, unique = true)
    private Integer singletonKey = 1;

    @Column(nullable = false, unique = true)
    private String username;

    private String password;

    @PrePersist
    @PreUpdate
    private void garantirSingletonKey() {
        this.singletonKey = 1;
    }
}