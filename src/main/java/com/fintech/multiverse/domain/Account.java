package com.fintech.multiverse.domain;

import lombok.Data;

@Data
public class Account {
    private String accountId;
    private String currency;
    private String accountType;
    private String accountSubType;
    private String description;
    private String nickName;
}
