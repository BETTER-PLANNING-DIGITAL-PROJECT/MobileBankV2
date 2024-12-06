package ibnk.intergrations.Tranzak.requestDtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class PaymentCallbackDto {

    private String name;
    private String version;
    private String eventType;
    private String merchantId;
    private String appId;
    private String resourceId;
    private Resource resource;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private String creationDateTime;
    private String webhookId;
    private String authKey;

    // Getters and setters for all fields
    // Constructor, if needed
    @Data
    public static class Resource {
        private String requestId;
        private int amount;
        private String currencyCode;
        private String description;
        private String mobileWalletNumber;
        private String status;
        private String transactionStatus;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        private String createdAt;
        private String mchTransactionRef;
        private String payerNote;
        private Double serviceDiscountAmount;
        private String receivingEntityName;
        private String transactionTag;
        private String transactionId;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        private String transactionTime;
        private String partnerTransactionId;
        private Payer payer;
        private Merchant merchant;
        private Links links;

    }

    @Data
    public static class Payer {
        private boolean isGuest;
        private String userId;
        private String name;
        private String paymentMethod;
        private String currencyCode;
        private String countryCode;
        private String accountId;
        private String accountName;
        private String email;
        private int amount;
        private int discount;
        private int fee;
        private int netAmountPaid;

        // Getters and setters for all fields
    }

    @Data
    public static class Merchant {
        private String currencyCode;
        private int amount;
        private int fee;
        private int netAmountReceived;
        private String accountId;

        // Getters and setters for all fields
    }

    @Data
    public static class Links {
        private String returnUrl;
        private String cancelUrl;


    }

}


