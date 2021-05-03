package com.maksimkarpov.account.repository;

import com.maksimkarpov.account.entity.Account;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends CrudRepository<Account,Long> { }
