package ibnk.dto.BankingDto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ibnk.service.BankingService.AccountService;
import lombok.*;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalanceDto {

    //    @ws_SenderAccount nvarchar (11) ,
    @JsonIgnore
    private int pc_OutLECT;
    @JsonIgnore
    private String pc_OutMSG;

    private Double validBalance;
    private Double clearBalance;
    private Double availableBalance;
    private String name;
    private String client;
    private String accountType;
    private String currency;
    private String accountNumber;

    public static AccountBalanceDto modelToDao(Map<String, Object> map) {
        AccountBalanceDto account = new AccountBalanceDto();
        account.setPc_OutLECT((Integer) map.get("OutStatus"));
        account.setPc_OutMSG((String) map.get("OutMessage"));
        account.setValidBalance((Double) map.get("ValidBalance"));
        account.setClearBalance((Double) map.get("RealBalance"));
        account.setAvailableBalance((Double) map.get("AvailableBalance"));
        account.setName((String) map.get("ClientName"));
        account.setClient((String) map.get("Client"));
        account.setAccountType((String) map.get("AccountType"));
        account.setCurrency((String) map.get("Currency"));
        account.setAccountNumber((String) map.get("LinkedAccount"));
        return account;
    }
    @Data
    public static class AccountDebitDto {

        @JsonIgnore
        private int pc_OutLECT;
        @JsonIgnore
        private int pc_OutId;
        @JsonIgnore
        private String pc_OutMSG;

        public static AccountBalanceDto.AccountDebitDto modelToDao(Map<String, Object> map) {
            AccountBalanceDto.AccountDebitDto account = new AccountDebitDto();
            account.setPc_OutLECT((Integer) map.get("pc_OutLECT"));
            account.setPc_OutId((Integer) map.get("pc_OutID"));
            account.setPc_OutMSG((String) map.get("pc_OutMSG"));
            return account;

        }
    }
}
