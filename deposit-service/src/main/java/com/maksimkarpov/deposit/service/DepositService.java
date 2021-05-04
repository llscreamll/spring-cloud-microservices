package com.maksimkarpov.deposit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maksimkarpov.deposit.controller.dto.DepositResponseDTO;
import com.maksimkarpov.deposit.entity.Deposit;
import com.maksimkarpov.deposit.exception.DepositServiceException;
import com.maksimkarpov.deposit.repository.DepositRepository;
import com.maksimkarpov.deposit.rest.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
public class DepositService {

    private static final String TOPIC_EXCHANGE_DEPOSIT = "js.deposit.notify.exchange";
    private static final String ROUTING_KEY_DEPOSIT = "js.key.deposit";

    private final DepositRepository repository;

    private final AccountServiceClient accountServiceClient;

    private final BillServiceClient billServiceClient;

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public DepositService(DepositRepository repository,
                          AccountServiceClient accountServiceClient,
                          BillServiceClient billServiceClient,
                          RabbitTemplate rabbitTemplate) {
        this.repository = repository;
        this.accountServiceClient = accountServiceClient;
        this.billServiceClient = billServiceClient;
        this.rabbitTemplate = rabbitTemplate;
    }

    public DepositResponseDTO deposit(Long accountId, Long billId, BigDecimal amount) {
        if (accountId == null && billId == null) {
            throw new DepositServiceException("Account is null and bill is null");
        }
        if (billId != null) {
            BillResponseDTO billResponseDTO = billServiceClient.getBillById(billId);

            BillRequestDTO billRequestDTO = getBillRequestDTO(amount, billResponseDTO);

            billServiceClient.update(billId, billRequestDTO);
            AccountResponseDTO accountResponseDTO = accountServiceClient.getAccountById(billResponseDTO.getAccountId());
            repository.save(new Deposit(amount, billId, OffsetDateTime.now(), accountResponseDTO.getEmail()));


            return createResponseDTO(amount, accountResponseDTO);
        }
        BillResponseDTO defaultBill = getDefaultBill(accountId);

        BillRequestDTO billRequestDTO = getBillRequestDTO(amount, defaultBill);
        billServiceClient.update(defaultBill.getBillId(),billRequestDTO);
        AccountResponseDTO account = accountServiceClient.getAccountById(accountId);
        repository.save(new Deposit(amount,defaultBill.getBillId(),OffsetDateTime.now(), account.getEmail()));

        return createResponseDTO(amount, account);

    }

    private DepositResponseDTO createResponseDTO(BigDecimal amount, AccountResponseDTO accountResponseDTO) {
        DepositResponseDTO depositResponseDTO = new DepositResponseDTO(amount,accountResponseDTO.getEmail());
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            rabbitTemplate.convertAndSend(TOPIC_EXCHANGE_DEPOSIT,
                    ROUTING_KEY_DEPOSIT,
                    objectMapper.writeValueAsString(depositResponseDTO));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new DepositServiceException("Can't send message to RabbitMQ");
        }
        return depositResponseDTO;
    }

    private BillRequestDTO getBillRequestDTO(BigDecimal amount, BillResponseDTO billResponseDTO) {
        BillRequestDTO billRequestDTO = new BillRequestDTO();
        billRequestDTO.setAccountId(billResponseDTO.getAccountId());
        billRequestDTO.setCreationDate(billResponseDTO.getCreationDate());
        billRequestDTO.setIsDefault(billResponseDTO.getIsDefault());
        billRequestDTO.setOverdraftEnabled(billRequestDTO.getOverdraftEnabled());
        billRequestDTO.setAmount(billResponseDTO.getAmount().add(amount));
        return billRequestDTO;
    }

    private BillResponseDTO getDefaultBill(Long accountId){
    return billServiceClient.getBillsByAccountId(accountId).stream()
            .filter(BillResponseDTO::getIsDefault)
            .findAny()
            .orElseThrow(()-> new DepositServiceException("Unable to find default bill for account : " + accountId));
    }

}
