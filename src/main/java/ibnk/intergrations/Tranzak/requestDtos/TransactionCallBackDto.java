package ibnk.intergrations.Tranzak.requestDtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TransactionCallBackDto {
    @JsonProperty("amount_received")
    private Double amountReceived;
    private int amount;
    private String status;
    private String type;
    @JsonProperty("user_reference")
    private String userReference;
    private String uuid;
    @JsonProperty("payment_method_code")
    private String paymentMethodCode;
    @JsonProperty("currency_code")
    private String currencyCode;
    @JsonProperty("country_code")
    private String countryCode;
    private String recipient;
    @JsonProperty("status_info")
    private StatusInfoDTO statusInfo;
    private String description;
    @JsonProperty("is_mock")
    private int isMock;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("updated_at")
    private String updatedAt;
    @JsonProperty("customer_id")
    private String customerId;
    @JsonProperty("customer_name")
    private String customerName;
    @JsonProperty("customer_address")
    private String customerAddress;
    @JsonProperty("customer_phone")
    private String customerPhone;
    @JsonProperty("provider_error_code")
    private String providerErrorCode;
    @JsonProperty("error_message")
    private String errorMessage;
    private String service;
    @JsonProperty("success_url")
    private String successUrl;
    @JsonProperty("cancel_url")
    private String cancelUrl;
    private Object extra;
    @JsonProperty("gateway_amount")
    private Double gatewayAmount;
    @JsonProperty("public_gateway_reference")
    private String publicGatewayReference;
    @JsonProperty("payment_promise")
    private Object paymentPromise;
    private CountryDTO country;
    private ChannelDTO channel;
    private CurrencyDTO currency;
    @JsonProperty("payment_method")
    private PaymentMethodDTO paymentMethod;
    @JsonProperty("gateway_currency")
    private Object gatewayCurrency;
    private UserDTO user;

    @Data
    public static class StatusInfoDTO {
        private String label;
        private String code;

    }

    @Data
    public static class CountryDTO {
        private String code;
        private String name;
        @JsonProperty("dial_code")
        private String dialCode;

    }

    @Data
    public static class ChannelDTO {
        private String code;
        @JsonProperty("is_mock")
        private int isMock;
    }

    @Data
    public static class CurrencyDTO {
        private String name;
        private String type;
        private String code;
        private int decimal;

    }
    @Data
    public static class PaymentMethodDTO {
        private String code;
        private String name;
    }
    @Data
    public static class UserDTO {
        private int id;
        private String name;
        private String organization;
        @JsonProperty("cancel_url")
        private String cancelUrl;
        @JsonProperty("success_url")
        private String successUrl;
        @JsonProperty("created_at")
        private String createdAt;
        private int dashboard;
        private String balance;
        private int admin;
        @JsonProperty("processing_enabled")
        private int processingEnabled;
    }
}

