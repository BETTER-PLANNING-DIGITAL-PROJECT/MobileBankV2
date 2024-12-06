package ibnk.dto.BankingDto;



import com.fasterxml.jackson.annotation.JsonIgnore;
import ibnk.models.banking.MobilePayment;
import lombok.Data;

@Data
public class MobilePaymentDto {
       private Double amount;
        private String account;
        private String client;
        private String telephone;
        @JsonIgnore
        private Double transactionCharge;
        private String status;
        private String type;
       @JsonIgnore
        private Double callBackReceived;
        private String trxNumber;
        private String date;
        @JsonIgnore
        private String application;

    public static MobilePaymentDto modelToDto(MobilePayment map)  {
        MobilePaymentDto mobilePaymentDto = new MobilePaymentDto();
        mobilePaymentDto.setDate(map.getDate().toString());
        mobilePaymentDto.setStatus(map.getStatus());
        mobilePaymentDto.setType(map.getType());
        mobilePaymentDto.setClient(map.getClient().getClientId());
        mobilePaymentDto.setTrxNumber(map.getTrxNumber());
        mobilePaymentDto.setAmount(map.getAmount());
        mobilePaymentDto.setAccount(map.getAccount().getAccountNumber());
//        mobilePaymentDto.setApplication();
        mobilePaymentDto.setTelephone(map.getTelephone());
        mobilePaymentDto.setCallBackReceived(map.getCallBackReceived());
        mobilePaymentDto.setTransactionCharge(map.getTransactionCharge());
        return mobilePaymentDto;
    }
}
