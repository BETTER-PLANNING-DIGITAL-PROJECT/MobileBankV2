package ibnk.dto.BankingDto.TransferModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payable {
    private String id;
    private String name;
    private float amount;
    private String payable_from;
    private String payable_until;
}
