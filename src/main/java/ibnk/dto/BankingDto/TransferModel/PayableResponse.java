package ibnk.dto.BankingDto.TransferModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayableResponse {
    private Boolean success ;

    private Integer code ;

    private String locale ;

    private String message ;

    private PayableResponseItems data ;
}
