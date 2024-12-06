package ibnk.repositories.banking;
import ibnk.dto.BankingDto.AccountBalanceDto;
import ibnk.models.banking.Account;
import ibnk.models.banking.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String> {
    @Query("SELECT act  FROM CpteClt act inner join Product p on act.accountProduct.id = p.id and  p.authorizeEbanking = 'Yes'    where  (act.client.clientId = :clientId or act.accountNumber = :clientId) and act.status = 'Actif' ")
    List<Account>getClientAccounts(@Param("clientId") String accountNumber);
    @Query("SELECT act FROM CpteClt act  inner join Product p on act.accountProduct.id = p.id and p.productType in('SAVING','CURRENT') and p.authorizeEbanking = 'Yes'  and p.authorizeDeposit= 'Yes' and p.authorizeWithdraw = 'Yes'  where  (act.client.clientId = :clientId or act.accountNumber = :clientId) and act.status = 'Actif' ")
    List<Account> getrpcAccounts(@Param("clientId") String accountNumber);

    @Query("SELECT act  FROM CpteClt act inner join Product p on act.accountProduct.id = p.id and  p.authorizeEbanking = 'Yes'  and p.authorizeDeposit= 'Yes'  where  (act.client.clientId = :clientId or act.accountNumber = :clientId) and act.status = 'Actif' ")
    List<Account>getAuthorizedDepositAccounts(@Param("clientId") String accountNumber);

    @Query("SELECT act  FROM CpteClt act inner join Product p on act.accountProduct.id = p.id and  p.authorizeEbanking = 'Yes'  and p.authorizeWithdraw = 'Yes'  where  (act.client.clientId = :clientId or act.accountNumber = :clientId) and act.status = 'Actif' ")
    List<Account>getAuthorizedWithdrawaltAccounts(@Param("clientId") String accountNumber);

    Optional<Account> findByAccountNumber(String account);
    boolean existsByClient(Client client);
    boolean existsByClientAndAccountNumber(Client client,String accountid);
    @Procedure(procedureName = "PS_RETURN_ACCOUNT_BALANCES")
    Object getAccountBalance(@Param("ws_SenderAccount") String accountNumber);

//    @Procedure(procedureName = "PS_RETURN_ACCOUNT_BALANCES")
//    AccountBalanceDto getAccountBalance(@Param("ws_SenderAccount") String accountNumber);
//    "TRANSFER TO " + item.getBeneficiary_account() + " WITH MEMO " + item.getMemo()
//    Date.valueOf(LocalDate.now()))
 @Procedure(procedureName = "PS_MAKE_ACCOUNT_TRANSFERT")
    AccountBalanceDto MAKEACCOUNTTRANSFERT(@Param("ws_Client")String wsClient,
                                        @Param( "ws_SenderAccount") String AccountId,
                                           @Param("ws_ReceiverAccount") String beneficiaryAccount,
                                           @Param("ws_Description") String description,
                                           @Param("ws_Ids")String Ids,
            @Param("ws_onlineopdate") String date,
            @Param("ws_Amount") String amount);

}
