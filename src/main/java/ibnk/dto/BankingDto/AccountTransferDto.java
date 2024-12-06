package ibnk.dto.BankingDto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountTransferDto {
    private String accountId;
    private String beneficiary_account;
    private String memo;
    private String ids;

    private String status;
    private String message;

    private String date;
    private float amount;
    private String client;
//    @JsonProperty(access = JsonProperty.Access.READ_ONLY)

    private String id_transaction;
    @JsonIgnore
    private int pc_OutLECT;
    @JsonIgnore
    private String pc_OutMSG;
    private Object fee;
    private Object tax;

    public static AccountTransferDto modelToDao(Map<String, Object> map) throws SQLException {
        AccountTransferDto accountTransferDto = new AccountTransferDto();
        accountTransferDto.setPc_OutLECT((Integer) map.get("pc_OutLECT"));
        accountTransferDto.setPc_OutMSG((String) map.get("pc_OutMSG"));
        accountTransferDto.setId_transaction((String) map.get("pc_OutID"));
        accountTransferDto.setFee( map.get("pc_fee"));
        accountTransferDto.setTax( map.get("pc_tax"));
        return accountTransferDto;
    }

    public static AccountTransferDto TransferToDao(Map<String, Object> map)  {
        AccountTransferDto accountTransferDto = new AccountTransferDto();
        accountTransferDto.setPc_OutLECT((Integer) map.get("pc_OutLECT"));
        accountTransferDto.setPc_OutMSG((String) map.get("pc_OutMSG"));
        return accountTransferDto;
    }


}
