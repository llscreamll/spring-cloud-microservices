package com.maksimkarpov.deposit.repository;


import com.maksimkarpov.deposit.entity.Deposit;
import org.springframework.data.repository.CrudRepository;

public interface DepositRepository extends CrudRepository<Deposit,Long> { }
