package ibnk.intergrations.Tranzak.responseDtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class InitiateCollectionResponse {
    @JsonProperty("data")
    private InitiatePay data;
    private boolean success;



    @Data
    public static class InitiatePay {
        private String requestId;
        private double amount;
        private String currencyCode;
        private String description;
        private String mobileWalletNumber;
        private String status;
        private String transactionStatus;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        private Date createdAt;
        @JsonProperty("mchTransactionRef")
        private String mchTransactionRef;
        private String appId;
        private String payerNote;
        private Double serviceDiscountAmount;
        private String receivingEntityName;
        private String transactionTag;
        private InitiateCollectionResponse.Links links;
    }

    @Data
    public static class Links {
        private String returnUrl;
        private String cancelUrl;
        private String paymentAuthUrl;

        // getters and setters
    }
}
