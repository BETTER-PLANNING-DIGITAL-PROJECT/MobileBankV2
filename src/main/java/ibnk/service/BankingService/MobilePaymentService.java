package ibnk.service.BankingService;

import ibnk.dto.BankingDto.AccountMvtDto;
import ibnk.dto.BankingDto.AccountTransferDto;
import ibnk.dto.BankingDto.PaymentDto;
import ibnk.dto.BankingDto.TransferModel.AccountCallback;
import ibnk.dto.BankingDto.TransferModel.MobilePayment;
import ibnk.models.internet.enums.Application;
import ibnk.tools.error.ResourceNotFoundException;
import ibnk.tools.error.ValidationException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class MobilePaymentService {
    @Qualifier("bankingJdbcTemplate")
   private final JdbcTemplate bankingJdbcTemplate;

    private SimpleJdbcCall account_callback;
    private SimpleJdbcCall account_mvt;
    private final PaymentService paymentService;

    @PostConstruct
    public void init() {
        this.account_callback = new SimpleJdbcCall(bankingJdbcTemplate)
                .withProcedureName("PS_MVT_ACCOUNTCALLBACK");
        this.account_mvt = new SimpleJdbcCall(bankingJdbcTemplate)
                .withProcedureName("PS_MVT_ACCOUNT");
    }

    public MobilePayment insertionMobilePayment(MobilePayment payement) throws  ResourceNotFoundException {
        String sql = "INSERT INTO MobilePayment(Uuid,Montant,CpteJumelle,Client,Telephone,Frais,Statut,PaymentGateWayUuid,Type,CallBackReceive,TypeOperation,TrxNumber,date,application,benefAccount) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
        Object[] params = new Object[]{payement.getUuid(),
                payement.getMontant(),
                payement.getCpteJumelle(),
                payement.getClient(),
                payement.getTelephone(),
                payement.getFrais(),
                payement.getStatus(),
                payement.getPaymentGatewaysUuid(),
                payement.getType(),
                payement.getCallBackReceive(),
                payement.getTypeOperation(),
                payement.getTrxNumber(),
                payement.getDate(),
                Application.MB.name(),
                payement.getBenefAccount()
        };
        int[] types = new int[]{java.sql.Types.VARCHAR, Types.FLOAT, java.sql.Types.VARCHAR, java.sql.Types.VARCHAR, java.sql.Types.VARCHAR,
                Types.FLOAT, java.sql.Types.VARCHAR, java.sql.Types.VARCHAR, java.sql.Types.VARCHAR, Types.BOOLEAN, java.sql.Types.VARCHAR,
                java.sql.Types.VARCHAR, java.sql.Types.VARCHAR,java.sql.Types.VARCHAR, Types.VARCHAR};
        int request = bankingJdbcTemplate.update(sql, params, types);

        if (request > 0) {
            return payement;
        } else {
            throw new ResourceNotFoundException("");
        }

    }

    /* update table payment method */
    public void updateMobilePayment(MobilePayment payment) throws ResourceNotFoundException {
        String sql = "UPDATE MobilePayment SET Statut=?, CallBackReceive=?, TrxNumber=?, Telephone=?, PaymentGatewayUuid=? WHERE Uuid=?";
        int rowsAffected = bankingJdbcTemplate.update(sql,
                new Object[]{
                        payment.getStatus(),
                        payment.getCallBackReceive(),
                        payment.getTrxNumber(),
                        payment.getTelephone(),
                        payment.getPaymentGatewaysUuid(),
                        payment.getUuid()
                },
                new int[]{
                        Types.VARCHAR,
                        Types.BOOLEAN,
                        Types.VARCHAR,
                        Types.VARCHAR,
                        Types.VARCHAR,
                        Types.VARCHAR
                });

        if (rowsAffected > 0) {
        } else {
            throw new ResourceNotFoundException("No payment found with UUID: " + payment.getUuid());
        }
    }

    public void account_callback(AccountCallback item) throws ValidationException {
        MapSqlParameterSource in = new MapSqlParameterSource()
                .addValue("ws_Account", item.getAccount())
                .addValue("pc_OutID", item.getTrxNumber())
                .addValue("ws_Telephone", item.getTelephone())
                .addValue("pc_OutMSG", Types.VARCHAR)
                .addValue("pc_OutLECT", Types.INTEGER);
        Map<String, Object> out = account_callback.execute(in);
        if (!out.get("pc_OutLECT").equals(0)) throw new ValidationException((String) out.get("pc_OutMSG"));
        AccountTransferDto.TransferToDao(out);
    }

    public List<MobilePayment> get_paymentUuid(String PaymentGatewayUuid) throws Exception {
        if (PaymentGatewayUuid == null || PaymentGatewayUuid.equals("")) {
            throw new Exception("PAYMENT UUID SHOULD NOT BE NULL");
        }
        String sqlQuery = "select * FROM MobilePayment where PaymentGatewayUuid=?";

        List<MobilePayment> result = new ArrayList<>();

        bankingJdbcTemplate.query(sqlQuery,
                new Object[]{PaymentGatewayUuid},
                new int[]{java.sql.Types.VARCHAR},
                (rs, rowNum) -> {
                    MobilePayment mobilePayment = new MobilePayment();
                    result.add(MobilePayment.modelToDao(mobilePayment, rs));
                    return null;
                });
        return result;
    }
    public List<MobilePayment> findByStatusAndApplication(String status,String application) {

        String sqlQuery = "select * FROM MobilePayment where Statut=? AND Application=? AND CallBackReceive=0";

        List<MobilePayment> result = new ArrayList<>();

        bankingJdbcTemplate.query(sqlQuery,
                new Object[]{status,application},
                new int[]{java.sql.Types.VARCHAR,java.sql.Types.VARCHAR},
                (rs, rowNum) -> {
                    MobilePayment mobilePayment = new MobilePayment();
                    result.add(MobilePayment.modelToDao(mobilePayment, rs));
                    return null;
                });
        return result;
    }
    public List<MobilePayment> findByUuidAndApplication(String PaymentGatewayUuid, String application) throws Exception {

//        String sqlQuery = "select * FROM MobilePayment where Telephone=? AND Application=? AND CallBackReceive=0";

        if (PaymentGatewayUuid == null || PaymentGatewayUuid.equals("")) {
            throw new Exception("PAYMENT UUID SHOULD NOT BE NULL");
        }
        String sqlQuery = "select * FROM MobilePayment where PaymentGatewayUuid=? AND Application=?";

        List<MobilePayment> result = new ArrayList<>();

        bankingJdbcTemplate.query(sqlQuery,
                new Object[]{PaymentGatewayUuid},
                new int[]{java.sql.Types.VARCHAR},
                (rs, rowNum) -> {
                    MobilePayment mobilePayment = new MobilePayment();
                    result.add(MobilePayment.modelToDao(mobilePayment, rs));
                    return null;
                });
        return result;
    }

    public List<MobilePayment> getPaymentBytUuidAndClient(String Uuid, String Client) throws Exception {
        if (Uuid == null || Uuid.equals("")) {
            throw new Exception("PAYMENT UUID SHOULD NOT BE NULL");
        }
        String sqlQuery = "select * FROM MobilePayment where Uuid=?";

        List<MobilePayment> result = new ArrayList<>();

        bankingJdbcTemplate.query(sqlQuery,
                new Object[]{Uuid.trim()},
                new int[]{java.sql.Types.VARCHAR},
                (rs, rowNum) -> {
                    MobilePayment mobilePayment = new MobilePayment();
                    return result.add(MobilePayment.modelToDao(mobilePayment, rs));
                });
        return result;
    }
    public List<MobilePayment> getPaymentTransactionStatus(String Uuid, String Client) throws Exception {
        if (Uuid == null || Uuid.equals("")) {
            throw new Exception("PAYMENT UUID SHOULD NOT BE NULL");
        }
        String sqlQuery = "select * FROM MobilePayment where PaymentGateWayUuid=?";

        List<MobilePayment> result = new ArrayList<>();

        bankingJdbcTemplate.query(sqlQuery,
                new Object[]{Uuid.trim()},
                new int[]{java.sql.Types.VARCHAR},
                (rs, rowNum) -> {
                    MobilePayment mobilePayment = new MobilePayment();
                    return result.add(MobilePayment.modelToDao(mobilePayment, rs));
                });
        return result;
    }

    public AccountMvtDto account_mvt(AccountMvtDto item) throws ValidationException {
        MapSqlParameterSource in = new MapSqlParameterSource()
                .addValue("ws_Account", item.getAccountId())
                .addValue("ws_Description", item.getDescription())
                .addValue("ws_Telephone", item.getPhoneNumber())
                .addValue("ws_Ids", item.getIds())
                .addValue("ws_onlineopdate", item.getDate())
                .addValue("ws_Amount", item.getAmount())
                .addValue("ws_Sens", item.getSens())
                .addValue("ws_TypeOp", item.getTypeOp());
        Map<String, Object> out = account_mvt.execute(in);
        if (!out.get("pc_OutLECT").equals(0)) throw new ValidationException((String) out.get("pc_OutMSG"));
        AccountMvtDto response = AccountMvtDto.TransferToDao(out);
        return response;

    }

    public String statusReload(String uuid) throws Exception {
        List<MobilePayment> pendingPayments = findByUuidAndApplication(uuid, Application.MB.name());
        Optional<MobilePayment> pending = pendingPayments.stream().findFirst();
        if(pending.isPresent())
            processPaymentStatus(pending.get());
        return "Success";
    }
    public void processPaymentStatus(MobilePayment payment) throws Exception {
        PaymentDto paymentDto = paymentService.transactionStatus(payment.getPaymentGatewaysUuid());
        String status = paymentDto.getData().getTransaction().getStatus().toString().trim();
        switch (status.trim()) {
            case "PENDING" -> updateMobilePayment(payment);
            case "FAILED", "CANCELLED" -> handleFailedOrCancelledPayment(payment);
            case "SUCCESS" -> handleSuccessfulPayment(payment);
            default -> {
            }
            // Handle unexpected status if necessary
        }
    }
    public void handleFailedOrCancelledPayment(MobilePayment payment) throws Exception {
        if (payment.getType().trim().equals("WITHDRAWAL")) {
            AccountCallback accountCallback = new AccountCallback();
            accountCallback.setAccount(payment.getCpteJumelle());
            accountCallback.setTrxNumber(payment.getTrxNumber());
            accountCallback.setTelephone(payment.getTelephone());

           account_callback(accountCallback);
        }
        payment.setCallBackReceive(true);
        updateMobilePayment(payment);
    }

    public void handleSuccessfulPayment(MobilePayment payment) throws Exception {
        if (payment.getType().trim().equals("DEPOSIT")) {
            AccountMvtDto accountMvt = createAccountMvtDto(payment);
            AccountMvtDto result = account_mvt(accountMvt);
            payment.setTrxNumber(result.getPc_OutID());

            if (result.getPc_OutLECT() != 0) {
                // Handle unsuccessful operation if needed
                return;
            }
        }
        payment.setCallBackReceive(true);
        updateMobilePayment(payment);
    }

    private AccountMvtDto createAccountMvtDto(MobilePayment payment) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String currentDate = dateFormat.format(new Date());

        AccountMvtDto accountMvt = new AccountMvtDto();
        accountMvt.setAccountId(payment.getCpteJumelle());
        accountMvt.setPhoneNumber(payment.getTelephone());
        accountMvt.setAmount(payment.getMontant());
        accountMvt.setDate(currentDate);
        accountMvt.setIds("");
        accountMvt.setTypeOp(payment.getTypeOperation());
        accountMvt.setSens(1);
        accountMvt.setDescription(payment.getDescription());

        return accountMvt;
    }
}
