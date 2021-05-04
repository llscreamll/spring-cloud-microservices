package com.maksimkarpov.deposit.controller.dto;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DepositRequestDTO {

    private Long accountId;

    private Long BillId;

    private BigDecimal amount;

}
