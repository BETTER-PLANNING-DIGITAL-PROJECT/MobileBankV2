package ibnk.dto.BankingDto.TransferModel;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
public class AccountCallback {
    private String account;
    private String TrxNumber;
    private String Telephone;
}
