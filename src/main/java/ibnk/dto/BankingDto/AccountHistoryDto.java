package ibnk.dto.BankingDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.ResultSet;
import java.sql.SQLException;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountHistoryDto {
    private double credit;
    private double debit;
    private String valueDate;
    private String operationDate;
    private String accountID;
    private String accountName;
    private Integer index;
    private String reference;
    private String agence;
    private String libAgence;
    private String representative;
    private String Txnno;
    private String currency;
    private String description;
    private String denomination;
    private double closingBalance;
    private double openingBalance;
    private String client;



    public static AccountHistoryDto modelToDao(AccountHistoryDto account, ResultSet map, double closingBalance,double openingBalance) throws SQLException {
        account.setCredit(map.getDouble("Credit"));
        account.setDenomination(map.getString("Denomination"));
        account.setAgence(map.getString("Agence"));
        account.setTxnno(map.getString("Txnno"));
        account.setLibAgence(map.getString("LibAgence"));
        account.setRepresentative(map.getString("representative"));
        account.setClient(map.getString("Client"));
//        account.setCurrency(map.getString("Devise"));
        account.setDebit(map.getDouble("Debit"));
        account.setValueDate( map.getString("DateValeur"));
        account.setOperationDate(map.getString("DateOperation"));
        account.setReference(map.getString("Txnno"));
        account.setAccountID(map.getString("CpteJumelle"));
        account.setAccountName(map.getString("LibProduct"));
        account.setIndex(map.getInt("serie"));
        account.setClosingBalance(closingBalance);
        account.setOpeningBalance(openingBalance);
        account.setDescription(map.getString("Description"));
        return account;
    }
}
