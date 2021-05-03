package com.maksimkarpov.account.service;

import com.maksimkarpov.account.entity.Account;
import com.maksimkarpov.account.exception.AccountNotFoundException;
import com.maksimkarpov.account.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class AccountService {
    private final AccountRepository accountRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account getAccountById(Long accountId){
      return accountRepository.findById(accountId)
              .orElseThrow(() -> new AccountNotFoundException("Unable to find account with id : " + accountId));
    }

    public Long createAccount(String name, String email, String phone, List<Long> bills) {
        Account account = new Account(name,email,phone, OffsetDateTime.now(),bills);
       return accountRepository.save(account).getAccountId();
    }

    public Account updateAccount(Long accountId,String name, String email, String phone, List<Long> bills){
        Account account = new Account();

        account.setAccountId(accountId);
        account.setName(name);
        account.setEmail(email);
        account.setPhone(phone);
        account.setBills(bills);
        return accountRepository.save(account);
    }

    public Account deleteAccount(Long accountId){
        Account deleteAccount = getAccountById(accountId);
        accountRepository.deleteById(accountId);
        return deleteAccount;
    }



}
