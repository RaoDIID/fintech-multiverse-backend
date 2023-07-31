package com.fintech.multiverse.domain;

import lombok.Data;

import java.util.Date;

@Data
public class Transaction {
    private String accountId;
    private String transactionId;
    private String status;
    private Long amount;
    private Long balance;
}
