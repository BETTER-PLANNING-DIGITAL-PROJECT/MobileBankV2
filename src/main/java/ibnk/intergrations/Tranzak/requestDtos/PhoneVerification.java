package ibnk.intergrations.Tranzak.requestDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PhoneVerification {
       String customTransactionId;

       @NotNull
       @NotBlank
       String accountHolderId;
}
