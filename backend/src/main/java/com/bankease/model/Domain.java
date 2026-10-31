package com.bankease.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

public final class Domain {
    private Domain() {}
    public enum Role { CUSTOMER, ADMIN, SUPPORT }
    public enum TransactionType { CREDIT, DEBIT, TRANSFER, BILL_PAYMENT }
    public record User(String id, String username, @JsonIgnore String password, String name, Role role, String accountId) {}
    public record Account(String id, String userId, String number, BigDecimal balance, String currency) {}
    public record Transaction(String id, String accountId, TransactionType type, BigDecimal amount, String description, Instant createdAt, String status) {}
    public record BillPayment(String id, String accountId, String biller, String reference, BigDecimal amount, Instant createdAt, String status) {}
    public record Loan(String id, String userId, BigDecimal amount, BigDecimal outstanding, String status, Instant createdAt) {}
    public record Investment(String id, String userId, String product, BigDecimal amount, String status, Instant createdAt) {}
    public record SupportTicket(String id, String userId, String subject, String message, String status, Instant createdAt) {}
    public record Dashboard(Account account, List<Transaction> recentTransactions, List<Loan> loans, List<Investment> investments) {}
}
