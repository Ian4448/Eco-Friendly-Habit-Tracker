package com.ecofriendly.ian.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller()
public class UserSummaryController {
    @GetMapping("/user/summary")
    public String userSummary() {
        return "usersummary";
    }
}
