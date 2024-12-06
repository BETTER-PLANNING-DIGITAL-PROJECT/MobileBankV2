package ibnk.intergrations.Tranzak.responseDtos;

import lombok.Data;

@Data
public class PhoneVerifyResponse {
    @Data
    public static class verify {
        String requestId;
        String customTransactionId;
        String accountHolderId;
        String verifiedName;
        String operatorName;
        String status;
        String createdAt;
        String errorCode;
        String errorMessage;
        boolean success;
    }
     verify data;
    String errorMsg;
    boolean success;
}
