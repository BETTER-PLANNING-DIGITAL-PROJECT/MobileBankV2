package ibnk.dto;

import lombok.Data;

@Data
public class SubscriptionDao {
    private String accountId;
    private String userLogin;
    private String packageCode;
    private Integer applyFee;

}
