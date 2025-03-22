package ibnk.repositories.banking;

import ibnk.models.banking.Client;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ClientMatriculRepository extends JpaRepository<Client, String> {

    @Modifying
    @Transactional
    @Query("UPDATE ClientBnk cl SET  cl.eBankPackage = :eBankPackage , cl.eBankPrimaryAccount = :eBankPrimaryAccount, cl.eBankSub = :eBankSub WHERE cl.clientId = :clientId")
    int updateClientEBankPackage(@Param("eBankPackage") String eBankPackage, @Param("eBankPrimaryAccount") String primaryAccountNumber, @Param("eBankSub") String eBankSub, @Param("clientId") String client);


    @Procedure(procedureName = "PS_VALIDATE_INTBANKING")
    Map<String, Object> validateIntBankingSubscription(@Param("ws_client") String clientId,
                                                       @Param("Type") Integer type,
                                                       @Param("takefee") Integer applyFee,
                                                       @Param("wEmploye") String employee,
                                                       @Param("Language") String language,
                                                       @Param("Computername") String computerName,
                                                       @Param("pc_OutLECT") String outCode,
                                                       @Param("pc_OutMSG") String message,
                                                       @Param("Password") String outPassword);

    @Query(value = """
        SELECT COUNT(c.client)
        FROM ClientBnk c
        WHERE c.client = CAST(:client AS VARCHAR)
          AND c.passwordmb = HASHBYTES('MD5', CAST(:password AS VARCHAR))
    """, nativeQuery = true)
    int verifyPasswordAndClient(@Param("client") String client, @Param("password") String password);
    Optional<Client> findByPhoneNumber (String numb);
}

