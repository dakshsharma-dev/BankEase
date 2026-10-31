package com.bankease.service;

import com.bankease.model.Domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class BankService {
    private final Map<String,User> users = new ConcurrentHashMap<>();
    private final Map<String,Account> accounts = new ConcurrentHashMap<>();
    private final Map<String,List<Transaction>> transactions = new ConcurrentHashMap<>();
    private final Map<String,List<Loan>> loans = new ConcurrentHashMap<>();
    private final Map<String,List<Investment>> investments = new ConcurrentHashMap<>();
    private final Map<String,List<BillPayment>> bills = new ConcurrentHashMap<>();
    private final Map<String,List<SupportTicket>> tickets = new ConcurrentHashMap<>();
    private final PasswordEncoder encoder; private final AtomicLong sequence = new AtomicLong(100);
    public BankService(PasswordEncoder encoder) { this.encoder=encoder; seed(); }
    private String id(String p) { return p + "-" + sequence.incrementAndGet(); }
    private void seed() {
        addUser("customer","customer123","Aarav Mehta",Role.CUSTOMER,"10000");
        addUser("admin","admin123","BankEase Admin",Role.ADMIN,"25000");
        addUser("support","support123","Support Desk",Role.SUPPORT,"50000");
    }
    private void addUser(String username,String password,String name,Role role,String balance) {
        String uid=id("usr"), aid=id("acc");
        users.put(uid,new User(uid,username,encoder.encode(password),name,role,aid));
        accounts.put(aid,new Account(aid,uid,"BE"+String.format("%010d",sequence.incrementAndGet()),new BigDecimal(balance),"INR"));
        transactions.put(aid, new CopyOnWriteArrayList<>());
    }
    public User userByUsername(String name) { return users.values().stream().filter(u->u.username().equals(name)).findFirst().orElse(null); }
    public User requireUser(String username) { User u=userByUsername(username); if(u==null) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"); return u; }
    public Account account(User u) { return accounts.get(u.accountId()); }
    public Dashboard dashboard(String username) { User u=requireUser(username); return new Dashboard(account(u), recent(u), loans.getOrDefault(u.id(),List.of()), investments.getOrDefault(u.id(),List.of())); }
    public List<Transaction> recent(User u) { return transactions.getOrDefault(u.accountId(),List.of()).stream().sorted(Comparator.comparing(Transaction::createdAt).reversed()).limit(20).toList(); }
    public List<BillPayment> bills(String username) {
        User u=requireUser(username);
        return List.copyOf(bills.getOrDefault(u.id(), List.of()));
    }
    public List<Loan> loans(String username) {
        User u=requireUser(username);
        return List.copyOf(loans.getOrDefault(u.id(), List.of()));
    }
    public List<Investment> investments(String username) {
        User u=requireUser(username);
        return List.copyOf(investments.getOrDefault(u.id(), List.of()));
    }
    private void ensureAmount(BigDecimal a) { if(a==null || a.signum()<=0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Amount must be positive"); }
    public synchronized Transaction transfer(String username,String destination,BigDecimal amount,String description) {
        ensureAmount(amount); User from=requireUser(username); Account source=account(from);
        Account target=accounts.values().stream().filter(a->a.number().equals(destination)||a.id().equals(destination)).findFirst().orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Destination account not found"));
        if(source.id().equals(target.id())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Source and destination must differ");
        if(source.balance().compareTo(amount)<0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Insufficient funds");
        accounts.put(source.id(),new Account(source.id(),source.userId(),source.number(),source.balance().subtract(amount),source.currency()));
        accounts.put(target.id(),new Account(target.id(),target.userId(),target.number(),target.balance().add(amount),target.currency()));
        Transaction out=new Transaction(id("txn"),source.id(),TransactionType.TRANSFER,amount,description==null?"Internal transfer":description,Instant.now(),"COMPLETED");
        Transaction in=new Transaction(id("txn"),target.id(),TransactionType.CREDIT,amount,"Transfer from "+source.number(),Instant.now(),"COMPLETED");
        transactions.get(source.id()).add(out); transactions.get(target.id()).add(in); return out;
    }
    public synchronized BillPayment bill(String username,String biller,String reference,BigDecimal amount) {
        ensureAmount(amount); User u=requireUser(username); Account a=account(u);
        if(a.balance().compareTo(amount)<0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Insufficient funds");
        accounts.put(a.id(),new Account(a.id(),a.userId(),a.number(),a.balance().subtract(amount),a.currency()));
        transactions.get(a.id()).add(new Transaction(id("txn"),a.id(),TransactionType.BILL_PAYMENT,amount,"Bill: "+biller,Instant.now(),"COMPLETED"));
        BillPayment payment=new BillPayment(id("bill"),a.id(),biller,reference,amount,Instant.now(),"PAID");
        bills.computeIfAbsent(u.id(),k->new CopyOnWriteArrayList<>()).add(payment);
        return payment;
    }
    public Loan loan(String username,BigDecimal amount) { ensureAmount(amount); User u=requireUser(username); Loan l=new Loan(id("loan"),u.id(),amount,amount,"PENDING",Instant.now()); loans.computeIfAbsent(u.id(),k->new CopyOnWriteArrayList<>()).add(l); return l; }
    public Investment invest(String username,String product,BigDecimal amount) { ensureAmount(amount); User u=requireUser(username); Account a=account(u); if(a.balance().compareTo(amount)<0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Insufficient funds"); accounts.put(a.id(),new Account(a.id(),a.userId(),a.number(),a.balance().subtract(amount),a.currency())); Investment i=new Investment(id("inv"),u.id(),product,amount,"ACTIVE",Instant.now()); investments.computeIfAbsent(u.id(),k->new CopyOnWriteArrayList<>()).add(i); return i; }
    public SupportTicket ticket(String username,String subject,String message) { User u=requireUser(username); SupportTicket t=new SupportTicket(id("ticket"),u.id(),subject,message,"OPEN",Instant.now()); tickets.computeIfAbsent(u.id(),k->new CopyOnWriteArrayList<>()).add(t); return t; }
    public Collection<User> allUsers() { return users.values(); }
    public Collection<SupportTicket> allTickets() { return tickets.values().stream().flatMap(Collection::stream).toList(); }
    public SupportTicket closeTicket(String id) { for(List<SupportTicket> list:tickets.values()) for(int i=0;i<list.size();i++) if(list.get(i).id().equals(id)){ SupportTicket n=new SupportTicket(id,list.get(i).userId(),list.get(i).subject(),list.get(i).message(),"RESOLVED",list.get(i).createdAt()); list.set(i,n); return n;} throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Ticket not found"); }
}
