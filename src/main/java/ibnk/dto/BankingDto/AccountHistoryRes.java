package ibnk.dto.BankingDto;

import lombok.Data;

import java.util.List;

@Data
public class AccountHistoryRes {

    List<AccountHistoryDto> historyDto;

    double openingBalance;
    String currency;

    double closingBalance;
}
