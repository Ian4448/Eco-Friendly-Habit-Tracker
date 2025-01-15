package com.ecofriendly.ian.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserForm {
    private String username;
    private String password;
    private Long userId;

    public UserForm(String username, String password, Long userId) {
        this.username = username;
        this.password = password;
        this.userId = userId;
    }
}
