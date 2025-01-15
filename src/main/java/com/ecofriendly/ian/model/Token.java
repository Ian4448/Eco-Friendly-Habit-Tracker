package com.ecofriendly.ian.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
public final class Token {

    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "\"user_id\"", nullable = false)
    private User user;

    @Column(nullable = false)
    @Getter
    private LocalDateTime expirationDate;

    public Token() {
    }

    public Token(String token, User user, LocalDateTime expirationDate) {
        this.token = token;
        this.user = user;
        this.expirationDate = expirationDate;
    }
}

