package com.shellhacks.ije.dao;

import com.shellhacks.ije.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenDAO extends JpaRepository<Token, Long> {
    Optional<Token> findByToken(String token);
}
