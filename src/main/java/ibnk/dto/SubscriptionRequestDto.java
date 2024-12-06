package ibnk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionRequestDto {
    private String accountId;
    private String accountType;
    private String phoneNumber;
    private String userLogin;
    private String password;
}
