package ibnk.dto.BankingDto.TransferModel;

import ibnk.models.internet.client.Subscriptions;
import ibnk.models.internet.enums.ChannelCode;
import ibnk.models.internet.enums.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitPayment {
    private Long amount;

    private String reference;

    private String country;

    private String recipient;

    private ChannelCode channel;

    private PaymentType type;

    private String customer_id;

    private String customer_name;

    private String customer_address;

    private String customer_phone;

    public static InitPayment AccountMvnToInitPay(MobilePayment  accountMvtDto, Subscriptions subscriptions){
        InitPayment init = new InitPayment();
        init.setAmount((long) accountMvtDto.getMontant());
        init.setCountry("cm");
        init.setRecipient(Integer.toString(Integer.parseInt(accountMvtDto.getTelephone())));
        init.setCustomer_id(subscriptions.getClientMatricul());
        init.setCustomer_name(subscriptions.getClientName());
        init.setCustomer_address("None");
        init.setCustomer_phone(subscriptions.getPhoneNumber());
        return init;
    }
}
