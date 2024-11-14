package com.ecofriendly.ian.dao;

import com.ecofriendly.ian.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenDAO extends JpaRepository<Token, Long> {
    Optional<Token> findByToken(String token);
}
