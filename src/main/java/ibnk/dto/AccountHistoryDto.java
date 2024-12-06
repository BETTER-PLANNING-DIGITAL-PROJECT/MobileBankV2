package ibnk.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AccountHistoryDto {
    private String accountId;
    private LocalDate openingDate;
    private String rangeSelector;
    private LocalDate closingDate;
    private Integer count = 100;
    private String transactions;
}
