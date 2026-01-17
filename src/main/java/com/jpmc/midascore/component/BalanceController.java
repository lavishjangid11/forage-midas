package com.jpmc.midascore.component;

import com.jpmc.midascore.foundation.Balance;
import com.jpmc.midascore.entity.User;
import com.jpmc.midascore.repository.UserRepository;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
public class BalanceController {

    private final UserRepository userRepository;

    public BalanceController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/balance")
    public Balance getBalance(@RequestParam Long userId) {

        return userRepository.findById(userId)
            .map(User::getBalance)
            .map(Balance::new)
            .orElse(new Balance(BigDecimal.ZERO));
    }
}
