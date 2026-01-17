package com.jpmc.midascore.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class TransactionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;

    @ManyToOne
    private User sender;

    @ManyToOne
    private User recipient;

    public TransactionRecord() {}

    public TransactionRecord(BigDecimal amount, User sender, User recipient) {
        this.amount = amount;
        this.sender = sender;
        this.recipient = recipient;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public User getSender() {
        return sender;
    }

    public User getRecipient() {
        return recipient;
    }
}
