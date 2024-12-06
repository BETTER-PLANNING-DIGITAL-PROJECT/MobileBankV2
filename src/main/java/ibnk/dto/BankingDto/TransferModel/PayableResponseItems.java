package ibnk.dto.BankingDto.TransferModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayableResponseItems {
    private ArrayList<Payable> items = new ArrayList<>();

}
