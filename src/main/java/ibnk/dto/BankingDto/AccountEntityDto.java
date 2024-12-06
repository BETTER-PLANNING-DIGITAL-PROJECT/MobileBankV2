package ibnk.dto.BankingDto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ibnk.models.banking.Account;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountEntityDto {
        private String AccountID;
        private String AccountName;

        @JsonIgnore
        private String ProductType;

        @JsonIgnore
        private String ACStatus;

        private String ClientName;


        private String authorizeDeposit;

        private String authorizeWithdrawal;

        @JsonIgnore
        private String ClientPrenom;

        @JsonIgnore
        private String Adresse1;

        @JsonIgnore
        private String ClientID;

        @JsonIgnore
        private String ClientType;

        @JsonIgnore
        private String client;

        private String Mobile;

        @JsonIgnore
        private String Phone1;

        @JsonIgnore
        private String email;

        @JsonIgnore
        private String Eaccount;

        @JsonIgnore
        private String MobileType;

        @JsonIgnore
        private String dateCreation;
        @JsonIgnore
        private String passwordOperation;

        @JsonIgnore
        private Integer outStatus;

        @JsonIgnore
        private String outMessage;

        @JsonIgnore
        private String OurBranchID;

        @JsonIgnore
        private String currency;

        @JsonIgnore
        private String BranchName;

        @JsonIgnore
        private String clientManager;

        @JsonIgnore
        private String placeOfBirth;

        @JsonIgnore
        private String dateOfBirth;

        @JsonIgnore
        private String IdentificationType;

        @JsonIgnore
        private String ProductID;

        @JsonIgnore
        private Double availableBalance;

        @JsonIgnore
        private String  libNationalite;

        @JsonIgnore
        private String poBox;

        @JsonIgnore
        private String town;

        @JsonIgnore
        private String fatherName;

        @JsonIgnore
        private String motherName;

        @JsonIgnore
        private String pContactAddress;

        @JsonIgnore
        private String pContactPhoneNumber;

        @JsonIgnore
        private String profession;

        @JsonIgnore
        private String IdentificationIssueDate;

        @JsonIgnore
        private String identificationExpireDate;

        @JsonIgnore
        private String placeIssueIdentification;

        @JsonIgnore
        private String phoneNumber2;

        @JsonIgnore
        private String EBankingSub;

        @JsonIgnore
        private int photoId;

        public static AccountEntityDto modelToDao(AccountEntityDto account, Account map) {
                account.setAuthorizeDeposit(map.getAccountProduct().getAuthorizeDeposit());
                account.setAuthorizeWithdrawal(map.getAccountProduct().getAuthorizeWithdraw());
                account.setProductType(map.getAccountProduct().getProductType().trim());
                account.setClient(map.getClient().getClientId());
                account.setCurrency(map.getCurrency().getShortName());
                account.setClientID(map.getClient().getClientId());
                account.setEmail(map.getClient().getEmail());
                account.setProfession(map.getClient().getProfession());
                account.setPContactPhoneNumber(map.getClient().getPContactPhoneNumber());
                account.setPlaceIssueIdentification(map.getClient().getPlaceIssueIdentification());
                account.setClientName(map.getAccountHolder());
                account.setAdresse1(map.getClient().getAddress1());
                account.setPhoneNumber2(map.getClient().getPhoneNumber2());
                account.setPContactAddress(map.getClient().getPContactAddress());
                account.setClientType(map.getClient().getClientType());
                account.setMobile(map.getClient().getPhoneNumber());
                account.setPhone1(map.getClient().getPhoneNumber2());
                account.setClientManager(map.getClient().getEmploye());
                account.setPlaceOfBirth(map.getClient().getPlaceOfBirth());
                account.setIdentificationType(map.getClient().getIdentificationType());
                account.setLibNationalite(map.getClient().getLibNationalite());
                account.setAccountID(map.getAccountNumber());
                account.setACStatus(map.getStatus());
                account.setProductID(String.valueOf(map.getAccountProduct().getId()));
                account.setAccountName(map.getAccountProduct().getProductName());
                account.setOurBranchID(map.getClient().getBranch());
                account.setBranchName(map.getClient().getBranchName());
                account.setMobileType(map.getClient().getEBankPackage());
                account.setEaccount(map.getClient().getEBankPrimaryAccount());
                account.setEBankingSub(map.getClient().getEBankSub());
                return account;
        }



}
