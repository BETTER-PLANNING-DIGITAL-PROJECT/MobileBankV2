package ibnk.dto.BankingDto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountMvtDto {
    private String accountId;
    private String phoneNumber;
    private String description;
    private String ids;
    private String date;
    private float amount;
    private int sens;
    private String typeOp;
    private String factureId;
    private String serviceClientId;
    private String currency;

    @JsonIgnore
    private int pc_OutLECT;
    @JsonIgnore
    private String pc_OutMSG;
    @JsonIgnore
    private String pc_OutID;
    @JsonIgnore
    private String pc_state;


    public static AccountMvtDto TransferToDao(Map<String, Object> map) {
        AccountMvtDto accountTransferDto = new AccountMvtDto();
        accountTransferDto.setPc_OutLECT((Integer) map.get("pc_OutLECT"));
        accountTransferDto.setPc_OutMSG((String) map.get("pc_OutMSG"));
        accountTransferDto.setPc_OutID((String)map.get("pc_OutID"));
        accountTransferDto.setPc_state((String)map.get("pc_state"));
        return accountTransferDto;
    }
}
