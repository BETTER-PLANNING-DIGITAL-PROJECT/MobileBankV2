package ibnk.dto.BankingDto;

import ibnk.dto.BankingDto.TransferModel.TransactionData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    private Boolean success;
    private Integer code;
    private String locale;
    private String message;
    private TransactionData data;
}
