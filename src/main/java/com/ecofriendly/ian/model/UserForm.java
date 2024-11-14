package com.ecofriendly.ian.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserForm {
    private String username;
    private String password;

    public UserForm(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
