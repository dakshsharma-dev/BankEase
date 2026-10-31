package com.bankease.security;

import com.bankease.service.BankService;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BankUserDetailsService implements UserDetailsService {
    private final BankService bank;
    public BankUserDetailsService(BankService bank) { this.bank = bank; }
    @Override public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.bankease.model.Domain.User u = bank.userByUsername(username);
        if (u == null) throw new UsernameNotFoundException(username);
        return org.springframework.security.core.userdetails.User.withUsername(u.username())
                .password(u.password()).roles(u.role().name()).build();
    }
}
