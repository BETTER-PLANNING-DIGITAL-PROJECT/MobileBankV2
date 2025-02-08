package ibnk.dto.BankingDto.TransferModel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ibnk.dto.BankingDto.AccountMvtDto;
import ibnk.dto.BankingDto.AccountTransferDto;
import ibnk.models.internet.client.Subscriptions;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
@Data
@AllArgsConstructor
public class MobilePayment {
    public MobilePayment() {
        this.Uuid = UUID.randomUUID().toString();
    }

    public String Uuid ;
    @JsonIgnore
    public float Montant;
    @JsonIgnore
    public String CpteJumelle;
    @JsonIgnore
    public String Client;
    @JsonIgnore
    public String Telephone;
    @JsonIgnore
    public float Frais;
    @JsonIgnore
    public float Tax;
    @JsonIgnore
    public String name;
    @JsonIgnore
    public String accountType;

    public String status;

    public String message;

    public String PaymentGatewaysUuid;
    @JsonIgnore
    public String Type;

    @JsonIgnore
    public String benefAccount;
    @JsonIgnore
    public Boolean CallBackReceive = false;
    @JsonIgnore
    public String TypeOperation;

    public String TrxNumber;
    @JsonIgnore
    public String date;
    @JsonIgnore
    public String description;

    public static MobilePayment AccountMvtToMobilePayWithdraw(AccountMvtDto accountMvtDto, Subscriptions subscriptions){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        String date = simpleDateFormat.format(new Date());
        MobilePayment mobilePayment = new MobilePayment();
        mobilePayment.setCpteJumelle(accountMvtDto.getAccountId());
        mobilePayment.setClient(subscriptions.getClientMatricul());
        mobilePayment.setMontant(accountMvtDto.getAmount());
        mobilePayment.setStatus("PENDING");
        mobilePayment.setTelephone(accountMvtDto.getPhoneNumber() );
        mobilePayment.setType("WITHDRAWAL");
        mobilePayment.setFrais(0);
        mobilePayment.setPaymentGatewaysUuid("");
        mobilePayment.setTypeOperation(accountMvtDto.getTypeOp());
        mobilePayment.setTrxNumber("");
        mobilePayment.date = date;
        mobilePayment.setUuid(UUID.randomUUID().toString());
        return mobilePayment;
    }
    public static MobilePayment AccountMvtToMobilePayDeposit(AccountMvtDto accountMvtDto, Subscriptions subscriptions){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        String date = simpleDateFormat.format(new Date());
        MobilePayment mobilePayment = new MobilePayment();
        mobilePayment.setCpteJumelle(accountMvtDto.getAccountId());
        mobilePayment.setClient(subscriptions.getClientMatricul());
        mobilePayment.setMontant(accountMvtDto.getAmount());
        mobilePayment.setStatus("PENDING");
        mobilePayment.setTelephone(accountMvtDto.getPhoneNumber() );
        mobilePayment.setType("DEPOSIT");
        mobilePayment.setFrais(0);
        mobilePayment.setPaymentGatewaysUuid("");
        mobilePayment.setTypeOperation(accountMvtDto.getTypeOp());
        mobilePayment.setTrxNumber("");
        mobilePayment.date = date;
        mobilePayment.setUuid(UUID.randomUUID().toString());
        return mobilePayment;
    }
    public static MobilePayment AccountTransferDtoToMobilePayDeposit(AccountTransferDto accountMvtDto, Subscriptions subscriptions){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        String date = simpleDateFormat.format(new Date());
        MobilePayment mobilePayment = new MobilePayment();
        mobilePayment.setCpteJumelle(accountMvtDto.getAccountId());
        mobilePayment.setBenefAccount(accountMvtDto.getBeneficiary_account());
        mobilePayment.setClient(subscriptions.getClientMatricul());
        mobilePayment.setMontant(accountMvtDto.getAmount());
        mobilePayment.setStatus("PENDING");
        mobilePayment.setTelephone(subscriptions.getPhoneNumber());
        mobilePayment.setType("TRANSFER");
        mobilePayment.setFrais(0);
        mobilePayment.setPaymentGatewaysUuid(accountMvtDto.getMemo());
        mobilePayment.setTypeOperation("INTERNAL");
        mobilePayment.setTrxNumber(accountMvtDto.getIds());
        mobilePayment.date = date;
        mobilePayment.setUuid(UUID.randomUUID().toString());
        return mobilePayment;
    }

    public static MobilePayment modelToDao(MobilePayment pay, ResultSet map) throws SQLException {
        pay.setUuid(map.getString("Uuid"));
        pay.setMontant(map.getFloat("Montant"));
        pay.setCpteJumelle(map.getString("CpteJumelle"));
        pay.setClient(map.getString("Client"));
        pay.setTelephone(map.getString("Telephone"));
        pay.setFrais(map.getFloat("Frais"));
        pay.setStatus(map.getString("Statut"));
        pay.setPaymentGatewaysUuid(map.getString("PaymentGatewayUuid"));
        pay.setType(map.getString("Type"));
        pay.setBenefAccount(map.getString("benefAccount"));
        pay.setCallBackReceive(map.getBoolean("CallBackReceive"));
        pay.setTypeOperation(map.getString("TypeOperation"));
        pay.setTrxNumber(map.getString("TrxNumber"));
        pay.setDate(map.getString("date"));
        return pay;
    }
}
