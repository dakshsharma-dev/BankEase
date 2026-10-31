package com.bankease.api;

import com.bankease.model.Domain.*;
import com.bankease.service.BankService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.*;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.*;

@RestController @RequestMapping("/api")
public class BankController {
    private final BankService bank; private final AuthenticationManager authenticationManager;
    public BankController(BankService bank, AuthenticationManager authenticationManager) { this.bank=bank; this.authenticationManager=authenticationManager; }
    public record Login(@NotBlank @Size(max=100) String username,@NotBlank @Size(max=200) String password) {}
    public record Transfer(@NotBlank @Size(max=64) String destination,@NotNull @Positive @Digits(integer=12,fraction=2) BigDecimal amount,@Size(max=200) String description) {}
    public record Bill(@NotBlank @Size(max=100) String biller,@NotBlank @Size(max=100) String reference,@NotNull @Positive @Digits(integer=12,fraction=2) BigDecimal amount) {}
    public record LoanRequest(@NotNull @Positive @Digits(integer=12,fraction=2) BigDecimal amount) {}
    public record InvestmentRequest(@NotBlank @Size(max=100) String product,@NotNull @Positive @Digits(integer=12,fraction=2) BigDecimal amount) {}
    public record TicketRequest(@NotBlank @Size(max=200) String subject,@NotBlank @Size(max=2000) String message) {}

    @GetMapping("/health") public Map<String,String> health() { return Map.of("status","ok"); }
    @PostMapping("/auth/login") public Map<String,Object> login(@Valid @RequestBody Login input,HttpServletRequest request,HttpServletResponse response) {
        String username=input.username().trim();
        Authentication result=authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username,input.password()));
        User user=bank.userByUsername(username);
        SecurityContext context=SecurityContextHolder.createEmptyContext(); context.setAuthentication(result); SecurityContextHolder.setContext(context);
        new HttpSessionSecurityContextRepository().saveContext(context,request,response);
        User u=user;
        return Map.of("message","Authenticated","user",Map.of("username",u.username(),"name",u.name(),"role",u.role(),"accountId",u.accountId()));
    }
    @PostMapping("/auth/logout") public Map<String,String> logout(HttpServletRequest request) { var session=request.getSession(false); if(session!=null) session.invalidate(); SecurityContextHolder.clearContext(); return Map.of("message","Logged out"); }
    @GetMapping("/me") public User me(Authentication a) { return bank.requireUser(a.getName()); }
    @GetMapping("/dashboard") public Dashboard dashboard(Authentication a) { return bank.dashboard(a.getName()); }
    @GetMapping("/accounts") public List<Account> accounts(Authentication a) { return List.of(bank.account(bank.requireUser(a.getName()))); }
    @GetMapping("/transactions") public List<Transaction> transactions(Authentication a) { return bank.recent(bank.requireUser(a.getName())); }
    @PostMapping("/transfers") public Transaction transfer(Authentication a,@Valid @RequestBody Transfer r) { return bank.transfer(a.getName(),r.destination(),r.amount(),r.description()); }
    @PostMapping("/bills/pay") public BillPayment bill(Authentication a,@Valid @RequestBody Bill r) { return bank.bill(a.getName(),r.biller(),r.reference(),r.amount()); }
    @GetMapping("/bills") public List<BillPayment> bills(Authentication a) { return bank.bills(a.getName()); }
    @PostMapping("/loans") public Loan loan(Authentication a,@Valid @RequestBody LoanRequest r) { return bank.loan(a.getName(),r.amount()); }
    @GetMapping("/loans") public List<Loan> loans(Authentication a) { return bank.loans(a.getName()); }
    @PostMapping("/investments") public Investment investment(Authentication a,@Valid @RequestBody InvestmentRequest r) { return bank.invest(a.getName(),r.product(),r.amount()); }
    @GetMapping("/investments") public List<Investment> investments(Authentication a) { return bank.investments(a.getName()); }
    @PostMapping("/support/tickets") public SupportTicket ticket(Authentication a,@Valid @RequestBody TicketRequest r) { return bank.ticket(a.getName(),r.subject(),r.message()); }
    @GetMapping("/admin/users") @PreAuthorize("hasRole('ADMIN')") public Collection<User> users() { return bank.allUsers(); }
    @GetMapping("/support/tickets") @PreAuthorize("hasAnyRole('ADMIN','SUPPORT')") public Collection<SupportTicket> tickets() { return bank.allTickets(); }
    @PatchMapping("/support/tickets/{id}/resolve") @PreAuthorize("hasAnyRole('ADMIN','SUPPORT')") public SupportTicket resolve(@PathVariable String id) { return bank.closeTicket(id); }
}
