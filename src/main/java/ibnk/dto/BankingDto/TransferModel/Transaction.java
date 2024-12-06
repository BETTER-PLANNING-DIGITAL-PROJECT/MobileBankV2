package ibnk.dto.BankingDto.TransferModel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ibnk.models.internet.enums.PaymentType;
import ibnk.models.internet.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class Transaction {
    private Integer amountReceived ;
    private Integer amount;
    private Status status;
    private PaymentType type;
    private String userReference;
    private String uuid;
    private String paymentMethodCode;
    private String currencyCode;

    private String countryCode;

    private String recipient;

    private StatusInfo statusInfo;

    private String description;
    private Boolean isMock ;
    private Date createdAt;
    private Date updatedAt;

    private Country country;

    private Channel channel;

    private Currency currency;

    private PaymentMethod payment_method;

    private String schemaType;

    private Object schemaDescription;




}
