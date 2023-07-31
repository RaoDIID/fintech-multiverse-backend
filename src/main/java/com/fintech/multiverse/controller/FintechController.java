package com.fintech.multiverse.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fintech.multiverse.domain.Account;
import com.fintech.multiverse.domain.Transaction;
import com.fintech.multiverse.service.FintechService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/boa")
public class FintechController {

    @Autowired
    private FintechService fintechService;

    @GetMapping(value="/accounts")
    public List<Account> getAccountDetails() throws URISyntaxException, JsonProcessingException {
        return fintechService.getAccounts();
    }

    @GetMapping(value="/transactions/{accountId}")
    public List<Transaction> getTransactionDetails(@PathVariable("accountId") String accountId) throws URISyntaxException, JsonProcessingException {
        return fintechService.getTransactions(accountId);
    }
}
