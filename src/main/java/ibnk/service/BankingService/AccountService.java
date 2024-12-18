package ibnk.service.BankingService;

import ibnk.dto.AccountHistoryDto;
import ibnk.dto.BankingDto.*;
import ibnk.dto.ClientRequestDto;
import ibnk.dto.DataTable;
import ibnk.models.banking.Account;
import ibnk.models.banking.CheckBookRequestHistEntity;
import ibnk.models.banking.MobileBeneficiairyEntity;
import ibnk.models.internet.StopPaymentHist;
import ibnk.models.internet.client.Subscriptions;
import ibnk.models.internet.enums.Status;
import ibnk.repositories.banking.*;
import ibnk.repositories.internet.StopPaymentHistRepository;
import ibnk.tools.TOOLS;
import ibnk.tools.error.ResourceNotFoundException;
import ibnk.tools.error.ValidationException;
import ibnk.tools.response.Tuple;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final StopPaymentHistRepository stopPaymentHistRepository;
    private final AccountRepository accountRepository;
    private final MobilePaymentRepository mobilePaymentRepository;
    private final MobileBeneficiaryRepository mobileBeneficiaryRepository;
    private final CheckBookRequestHistEntityRepository checkBookRequestHistEntityRepository;
    @Qualifier("bankingJdbcTemplate")
    private final JdbcTemplate bankingJdbcTemplate;
    private SimpleJdbcCall accountBalancesCall;
    private SimpleJdbcCall clientInsertCall;
    private SimpleJdbcCall accountTransferCall;
    private SimpleJdbcCall amountBillingOptionWithVAT;
    private SimpleJdbcCall mobileLimit;
    private SimpleJdbcCall comptabliteMb;

    @PostConstruct
    public void init() {
        this.accountBalancesCall = new SimpleJdbcCall(bankingJdbcTemplate)
                .withProcedureName("IBSP_GET_ACCOUNT_BALANCES");
        this.clientInsertCall = new SimpleJdbcCall(bankingJdbcTemplate)
                .withProcedureName("ClientBnk_Insert");


        this.accountTransferCall = new SimpleJdbcCall(bankingJdbcTemplate)
                .withProcedureName("PS_MAKE_ACCOUNT_TRANSFERT");
//        PS_MAKE_ACCOUNT_TRANSFER_IBNK

        this.amountBillingOptionWithVAT = new SimpleJdbcCall(bankingJdbcTemplate)
                .withProcedureName("RetAmountBillingOptionWithVAT");
        this.mobileLimit = new SimpleJdbcCall(bankingJdbcTemplate)
                .withProcedureName("PS_MOBILE_LIMIT");
        this.comptabliteMb = new SimpleJdbcCall(bankingJdbcTemplate)
                .withProcedureName("PS_COMPTABLITE_MBANKING");
//        IBS_GET_BILLING_WITH_VAT

    }

    public DataTable allTransactions(int pageNumber, int pageSize, String sortDirection, String sortProperty, String propertyValue, String status, String fromDateString, String toDateString) {
        LocalDateTime from = null;
        LocalDateTime to = null;
        ibnk.models.banking.MobilePayment exampleRequest = new ibnk.models.banking.MobilePayment();

        // Use Java Reflection to set the property dynamically
        try {
            BeanUtils.setProperty(exampleRequest, sortProperty, propertyValue.trim());
            BeanUtils.setProperty(exampleRequest, "application".trim(), "InternetBanking".trim());
            BeanUtils.setProperty(exampleRequest, "status".trim(), status.trim());
        } catch (Exception e) {
            // Handle reflection exception
            e.printStackTrace();
            return null; // or handle the error accordingly
        }
        // Create ExampleMatcher to match by ignoring case and matching substrings
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.STARTING);

        // Create Example with the dynamically created example user and the matcher
        Example<ibnk.models.banking.MobilePayment> example = Example.of(exampleRequest, matcher);

        Pageable pageList = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.valueOf(sortDirection.toUpperCase()), sortProperty));
        // Convert from and to dates if provided
        if (fromDateString != null && !fromDateString.isEmpty()) {
            from = LocalDateTime.parse(fromDateString);
        }
        if (toDateString != null && !toDateString.isEmpty()) {
            to = LocalDateTime.parse(toDateString);
        }
        Page<ibnk.models.banking.MobilePayment> pages = mobilePaymentRepository.findAllWithinDateRange(from, to, pageList);
        DataTable table = new DataTable();
        List<MobilePaymentDto> requests = pages.getContent().stream().filter(pages.getContent()::contains).map(MobilePaymentDto::modelToDto).toList();
        table.setTotalPages(pages.getTotalPages());
        table.setTotalElements(pages.getTotalElements());
        table.setData(requests);
        return table;
    }


    // TODO CLIENT ACCOUNT REQUEST NOT IMPLEMENTED
    public String ClientInsert(ClientRequestDto.BasicRequestDto dto) throws Exception {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        MapSqlParameterSource in = new MapSqlParameterSource()
//                .addValue("Agence", dto.getA)
//                .addValue("Client", dao.getDrAccountId())
                .addValue("Nom", dto.getName())
                .addValue("Prenom", dto.getSurname())
                .addValue("Adresse1", dto.getAddress1())
                .addValue("Adresse2", dto.getAddress2())
                .addValue("BP", dto.getPoBox())
                .addValue("Ville", dto.getTown())
                .addValue("Pays", dto.getCountryOfResidence())
                .addValue("Qualite", dto.getGender().equals("MALE") ? "Mr" : "Mme")
                .addValue("TypeClient", dto.getCustomerType())
                .addValue("Telephone1", dto.getPhoneNumber1())
                .addValue("Telephone2", dto.getPhoneNumber2())
                .addValue("LieuNaissance", dto.getTown())
                .addValue("DateCreation", LocalDate.now().format(format))
                .addValue("DateNaissance", dto.getBirthDate())
                .addValue("email", dto.getEmail())
                .addValue("CatClient", dto.getAccountType())
                .addValue("Nationalite", dto.getNationality())
                .addValue("ComputerName", TOOLS.getMotherboardSerialNumber());
        Map<String, Object> out = clientInsertCall.execute(in);
        Double lect;
        String message;
        lect = (Double) out.get("lect");
        message = (String) out.get("ErrMsg");
        if (lect != 0) throw new Exception(message);
        return "Success";
//
    }


    public AccountBalanceDto findAccountBalancesWithAccount(String accountNumber, String client) throws ValidationException {
        MapSqlParameterSource in = new MapSqlParameterSource()
                .addValue("AccountNumber", accountNumber )
                .addValue("Client", client);
        try {
            Map<String, Object> out = accountBalancesCall.execute(in);
            if ((Integer) out.get("OutStatus") != 0) return null;
            return AccountBalanceDto.modelToDao(out);
        } catch (Exception e) {
            throw new ValidationException(e.getMessage());
        }
    }


    public AccountTransferDto account_transfer(AccountTransferDto item, Subscriptions subscription) throws SQLException, ValidationException {
        MapSqlParameterSource in = new MapSqlParameterSource()
                .addValue("ws_Client", subscription.getClientMatricul())
                .addValue("ws_SenderAccount", item.getAccountId())
                .addValue("ws_ReceiverAccount", item.getBeneficiary_account())
                .addValue("ws_Description", "TRANSFER TO " + item.getBeneficiary_account() + " WITH MEMO " + item.getMemo())
                .addValue("ws_Ids", item.getIds())
                .addValue("ws_onlineopdate", Date.valueOf(LocalDate.now()))
                .addValue("ws_Amount", item.getAmount());
        item.setDate(LocalDate.now().toString());

        Map<String, Object> out = accountTransferCall.execute(in);
        AccountTransferDto response = AccountTransferDto.modelToDao(out);
        if (response.getPc_OutLECT() != 200) throw new ValidationException(response.getPc_OutMSG());
        return response;
    }
    public AccountMvtDto mobileLimit(AccountMvtDto item, Subscriptions subscription) throws SQLException, ValidationException {
        MapSqlParameterSource in = new MapSqlParameterSource()
                .addValue("ws_Client", subscription.getClientMatricul())
                .addValue("ws_Amount", item.getAmount());
//        item.setDate(LocalDate.now().toString());
        Map<String, Object> out = mobileLimit.execute(in);
        AccountMvtDto response = AccountMvtDto.TransferToDao(out);
        if (response.getPc_OutLECT() != 0) throw new ValidationException(response.getPc_OutMSG());
        return response;
    }
    public AccountMvtDto ServiceCharge(AccountMvtDto item, Subscriptions subscription, HttpServletRequest request) throws  ValidationException {
        MapSqlParameterSource in = new MapSqlParameterSource()
                .addValue("Eaccount", subscription.getPrimaryAccount())
                .addValue("ws_TypeOp", item.getTypeOp())
                .addValue("Language",request.getLocale().getLanguage());
//        item.setDate(LocalDate.now().toString());
        Map<String, Object> out = comptabliteMb.execute(in);
        AccountMvtDto response = AccountMvtDto.TransferToDao(out);
        if (response.getPc_OutLECT() != 0) throw new ValidationException(response.getPc_OutMSG());
        return response;
    }

    public BillingListDto amountBillingOptionWithVAT(BillingListDto item, boolean isDebit, Subscriptions subscriptions) throws ValidationException {
        MapSqlParameterSource in = new MapSqlParameterSource()
                .addValue("Pd_ServerDate", item.getPd_ServerDate())
                .addValue("Pc_PrincipAccount", item.getPc_PrincipAccount())
                .addValue("Pc_SlaveAccount", item.getPc_PrincipAccount())
                .addValue("Pc_TypeOp", item.getPc_TypeOp())
                .addValue("Pc_CodeOp", item.getPc_CodeOp())
                .addValue("SvMontant", item.getSvMontant())
                .addValue("SvParaTx", item.getSvParaTx())
                .addValue("Language", item.getLanguage());
        Map<String, Object> out = amountBillingOptionWithVAT.execute(in);
        if ((Integer) out.get("lect") != 0) throw new ValidationException((String) out.get("ErrMsg"));

        BillingListDto billing = BillingListDto.modeltodto(out);
        if (isDebit) {
            AccountBalanceDto balance = findAccountBalancesWithAccount(item.getPc_PrincipAccount(), subscriptions.getClientMatricul());
            Double total = billing.getRetAmount() + billing.getMntTva() + item.getSvMontant();
            if (balance.getAvailableBalance() < total)
                throw new ValidationException("Insufficient balance - " + item.getPc_PrincipAccount());
        }
        return billing;
    }


    public List<BeneficiaryDto> findBeneficiaryByClientId(String clientId) {
        String sql = "select mb.id,mb.agence,mb.donneur,mb.beneficiaire,  \n" +
                "                mb.telephone,mb.cni, mb.nom, mb.onInstitution \n" +
                "                FROM  MobileBeneficiairy mb WHERE mb.client = ? AND mb.status=? ";
        List<BeneficiaryDto> result = new ArrayList<>();
        bankingJdbcTemplate.query(sql, new Object[]{clientId,Status.APPROVED.name()}, new int[]{java.sql.Types.VARCHAR,java.sql.Types.VARCHAR}, (rs, rowNum) -> {
            BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
            result.add(BeneficiaryDto.modelToDao(beneficiaryDto, rs));
            return null;
        });
        return result;
    }

    public MobileBeneficiairyEntity findBeneficiaryByUuid(String uuid) throws ValidationException {
        return mobileBeneficiaryRepository.findByUuid(uuid).orElseThrow(() -> new ValidationException("Empty"));
    }

    public List<BeneficiaryDto> checkClientBeneficiary(String clientId, String benefAccountId) {
        String sql = "select cpt.agence, mb.donneur,  clt.Telephone1 telephone, clt.CNIPass cni, cpt.LibClient nom, cpt.CpteJumelle beneficiaire , mb.id, mb.onInstitution \n" +
                "FROM  CpteClt cpt \n" +
                "INNER JOIN ClientBnk clt on clt.client = cpt.Client\n" +
                "LEFT JOIN MobileBeneficiairy mb ON mb.client = ? and cpt.CpteJumelle = mb.beneficiaire\n" +
                "WHERE cpt.CpteJumelle = ?";
        List<BeneficiaryDto> result = new ArrayList<>();
        bankingJdbcTemplate.query(sql, new Object[]{clientId, benefAccountId}, new int[]{java.sql.Types.VARCHAR, java.sql.Types.VARCHAR}, (rs, rowNum) -> {
            BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
            result.add(BeneficiaryDto.modelToDao(beneficiaryDto, rs));
            return null;
        });
        return result;
    }

    public BeneficiaryDto saveBeneficiary(BeneficiaryDto beneficiaryDto, Subscriptions subscriptions) throws ValidationException {
        Object[] params = null;
        Optional<BeneficiaryDto> beneficiaryInfo  = checkClientBeneficiary(subscriptions.getClientMatricul(), beneficiaryDto.getBenefactorAccountNumber())
                .stream()
                .findFirst();
        if (!beneficiaryDto.getMobile()) {

            if (beneficiaryInfo.isEmpty()) {
                throw new ValidationException("account-not-found");
            }
           Optional<MobileBeneficiairyEntity> fnd = mobileBeneficiaryRepository.findByClientAndBeneficiary(subscriptions.getClientMatricul(), beneficiaryDto.getBenefactorAccountNumber());
            if (fnd.isPresent()) {
                throw new ValidationException("beneficiary-exist");
            }
            params = new Object[]{subscriptions.getBranchCode(),
                    subscriptions.getClientMatricul(),
                    null,
                    beneficiaryDto.getBenefactorAccountNumber(),
                    beneficiaryInfo.get().getPhoneNumber(),
                    beneficiaryInfo.get().getName(),
                    "yes",
                    Status.PENDING.name(),
                    beneficiaryDto.getUuid()};
        }
        String insertQuery = "INSERT INTO MobileBeneficiairy (" +
                "[agence]\n" +
                "\t\t\t\t   ,[client]\n" +
                "\t\t\t\t   ,[donneur]\n" +
                "\t\t\t\t   ,[beneficiaire]\n" +
                "\t\t\t\t   ,[telephone]\n" +
                "\t\t\t\t   ,[nom]\n" +
                "\t\t\t\t   ,[onInstitution]\n" +
                "\t\t\t\t   ,[status]\n" +
                "\t\t\t\t   ,[uuid]) " +
                "VALUES (?,?,?,?,?,?,?,?,?)";

        if (beneficiaryDto.getMobile()) {
            Optional <MobileBeneficiairyEntity> mbfnd = mobileBeneficiaryRepository.findByClientAndBeneficiaryAndTelephoneAndStatus(subscriptions.getClientMatricul(),
            "mobile",beneficiaryDto.getPhoneNumber(),Status.PENDING.name());
            if(mbfnd.isPresent()){
                beneficiaryDto.setUuid(mbfnd.get().getUuid());
                return beneficiaryDto;
            }
          Optional <MobileBeneficiairyEntity> mb = mobileBeneficiaryRepository.findByClientAndBeneficiaryAndTelephone(subscriptions.getClientMatricul(),"mobile",beneficiaryDto.getPhoneNumber());
           if(mb.isPresent()){
                throw new ValidationException("mobile-contact-already-exit");
            }

            params = new Object[]{
                    subscriptions.getBranchCode(),
                    subscriptions.getClientMatricul(),
                    subscriptions.getPrimaryAccount(),
                    "mobile",
                    beneficiaryDto.getPhoneNumber(),
                    beneficiaryDto.getName(),
                    "yes",
                    Status.PENDING.name(),
                beneficiaryDto.getUuid() };
        }
        int[] types = new int[]{java.sql.Types.VARCHAR,
                java.sql.Types.VARCHAR,
                java.sql.Types.VARCHAR,
                java.sql.Types.VARCHAR,
                java.sql.Types.VARCHAR,
                java.sql.Types.VARCHAR,
                java.sql.Types.VARCHAR,
                java.sql.Types.VARCHAR,
                java.sql.Types.VARCHAR};
        try {
            bankingJdbcTemplate.update(insertQuery, params, types);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ValidationException("Not_Available");
        }
        return beneficiaryDto;
    }

    public BeneficiaryDto deleteBeneficiary(Integer beneficiaryId, Subscriptions user) throws SQLException, ValidationException {
        SimpleJdbcCall call = new SimpleJdbcCall(bankingJdbcTemplate).withProcedureName("MB_Delete_Beneficiairy");
        MapSqlParameterSource in = new MapSqlParameterSource()
                .addValue("idTransaction", beneficiaryId)
                .addValue("client", user.getClientMatricul());
        Map<String, Object> out = call.execute(in);
        BeneficiaryDto accountBalanceDto = BeneficiaryDto.modelToDelete(out);
        if (accountBalanceDto.getLect() != 200)
            throw new ValidationException(accountBalanceDto.getErrMsg());
        return accountBalanceDto;
    }

    public List<AccountEntityDto> findClientAccounts(String clientId) {
        return accountRepository.getClientAccounts(clientId).stream().map((account ->
                AccountEntityDto.modelToDao(new AccountEntityDto(), account))).toList();
    }

    public List<AccountEntityDto> findClientAccountsByOperation(String clientId, String operation) {
        List<Account> accounts = new ArrayList<>();
        if(Objects.equals(operation, "debit")) {
            accounts = accountRepository.getAuthorizedWithdrawaltAccounts(clientId);
        } else if (Objects.equals(operation, "credit")) {
            accounts = accountRepository.getAuthorizedDepositAccounts(clientId);
        } else {
            accounts = accountRepository.getClientAccounts(clientId);
        }
        return accounts.stream().map((account ->
                AccountEntityDto.modelToDao(new AccountEntityDto(), account))).toList();
    }

    public List<MobileBankConfigDto> findMbConfig() throws SQLException {
        String sql = "SELECT Code\n" +
                "      ,Description\n" +
                "      ,MaxPerDayToMobile\n" +
                "      ,NbMaxPerDayToMobile\n" +
                "      ,MaxPeMonthToMobile\n" +
                "      ,MaxPeWeekToMobile\n" +
                "      ,MaxAmtToMobile\n" +
                "      ,MaxAmtFromMobile\n" +
                "      ,MaxAmtTrfToAccount\n" +
                "      ,MaxPerDayTrfToAccount\n" +
                "      ,MinAmtTrfToAccount\n" +
                "      ,MinAmtToMobile\n" +
                "      ,MonthlyFee\n" +
                "      ,FraisAbonnement\n" +
                "  FROM MobileBankConfiguration";
        List<Map<String, Object>> resultSet = bankingJdbcTemplate.queryForList(sql);
        List<MobileBankConfigDto> result = new ArrayList<>();
        for (Map<String, Object> set : resultSet) {
            MobileBankConfigDto mobileBankConfigDto = new MobileBankConfigDto();
            result.add(MobileBankConfigDto.modelToDaa(mobileBankConfigDto, set));
        }
        return result;
    }

    public MobileBankConfigDto findMbConfigByCode(String code) throws ResourceNotFoundException {
        String sql = "SELECT Code\n" +
                "      ,Description\n" +
                "      ,MaxPerDayToMobile\n" +
                "      ,NbMaxPerDayToMobile\n" +
                "      ,MaxPeMonthToMobile\n" +
                "      ,MaxPeWeekToMobile\n" +
                "      ,MaxAmtToMobile\n" +
                "      ,MaxAmtFromMobile\n" +
                "      ,MaxAmtTrfToAccount\n" +
                "      ,MaxPerDayTrfToAccount\n" +
                "      ,MinAmtTrfToAccount\n" +
                "      ,MinAmtToMobile\n" +
                "      ,MonthlyFee\n" +
                "      ,FraisAbonnement\n" +
                "  FROM MobileBankConfiguration" +
                " WHERE Code = ?";
        List<MobileBankConfigDto> result = new ArrayList<>();
        bankingJdbcTemplate.query(sql, new Object[]{code}, new int[]{java.sql.Types.VARCHAR}, (rs, rowNum) -> {
            MobileBankConfigDto mobileBankConfigDto = new MobileBankConfigDto();
            result.add(MobileBankConfigDto.modelToDao(mobileBankConfigDto, rs));
            return null;
        });
        return result.stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("package_not_found"));
    }

    public double GetBalanceAtDate(String accountId, String date, String client) {
        String sql = "select SUM(cptHist.Credit - cptHist.Debit ) balance  from CpteCltHist cptHist\r\n" + //
                "where  cptHist.CpteJumelle = ? and cptHist.client = ?  and cptHist.DateOperation <= ? ";

        final double[] balance = {0.0}; // Initialize with a default value
        bankingJdbcTemplate.query(sql, new Object[]{accountId, client, date}, new int[]{java.sql.Types.VARCHAR, java.sql.Types.VARCHAR, java.sql.Types.VARCHAR}, (rs, rowNum) -> {
            MobileBankConfigDto mobileBankConfigDto = new MobileBankConfigDto();
            balance[0] = rs.getDouble("balance");
            return null;
        });
        return balance[0];
    }

//    public String deleteMobileBeneficiary(Long id) {
//        mobileBeneficiaryRepository.deleteById(id);
//        return "Deleted";
//    }

    public AccountHistoryRes clientAccountHistory(AccountHistoryDto dto, Subscriptions sub) throws ResourceNotFoundException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        double OpeningBalance = GetBalanceAtDate(dto.getAccountId(), dto.getOpeningDate().format(formatter), sub.getClientMatricul());
        final double[] ClosingBallance = { OpeningBalance };
        AccountHistoryRes accountHistoryRes = new AccountHistoryRes();
        final List<ibnk.dto.BankingDto.AccountHistoryDto>[] result = new List[]{new ArrayList<>()};

        String sql = "SELECT cptHist.Denomination,cptHist.Txnno,cptHist.representative,cptHist.Devise,cptHist.LibAgence,cptHist.Agence,cptHist.Description, cptHist.Txnno, cptHist.serie, cptHist.CpteJumelle ,  cpt.LibCatCpte as LibProduct,  cptHist.Credit,cptHist.Client, cptHist.Debit, cptHist.DateValeur, cptHist.DateOperation from CpteCltHist cptHist\r\n" + //
                "inner join CpteClt cpt on cpt.CpteJumelle = cptHist.CpteJumelle " +//
                "WHERE cptHist.CpteJumelle = ? and cptHist.client = ?  and cptHist.DateOperation BETWEEN ? and ?  ORDER BY cptHist.DateOperation ASC, cptHist.serie ASC";
        bankingJdbcTemplate.query(sql,
                new Object[]{dto.getAccountId(), sub.getClientMatricul(), dto.getOpeningDate().format(formatter), dto.getClosingDate().format(formatter)},
                new int[]{java.sql.Types.VARCHAR, java.sql.Types.VARCHAR, java.sql.Types.VARCHAR, java.sql.Types.VARCHAR},
                (rs, rowNum) -> {
                    ibnk.dto.BankingDto.AccountHistoryDto accountHistoryDto = new ibnk.dto.BankingDto.AccountHistoryDto();
                    ClosingBallance[0] = ClosingBallance[0] + (rs.getDouble("Credit") - rs.getDouble("Debit"));
                    result[0].add(ibnk.dto.BankingDto.AccountHistoryDto.modelToDao(accountHistoryDto, rs, ClosingBallance[0], OpeningBalance));
                    return null;
                });
        if (Objects.equals(dto.getTransactions(), "CREDIT")) {
            result[0] = result[0].stream().filter(accountHistory -> accountHistory.getCredit() > 0).toList();
        } else if (Objects.equals(dto.getTransactions(), "DEBIT")) {
            result[0] = result[0].stream().filter(accountHistory -> accountHistory.getDebit() > 0).toList();
        }
//        AccountEntityDto ent = findClientAccounts(dto.getAccountId()).stream().findFirst().orElseThrow(() -> new ResourceNotFoundException("package_not_found"));
        accountHistoryRes.setHistoryDto(result[0]);
        accountHistoryRes.setOpeningBalance(OpeningBalance);
        accountHistoryRes.setClosingBalance(ClosingBallance[0]);
//        accountHistoryRes.setCurrency(ent.getCurrency());

        return accountHistoryRes;
    }

    public List<ibnk.dto.BankingDto.AccountHistoryDto> getClientActivity(AccountHistoryDto dao, String ClientMatricul) throws ResourceNotFoundException {
        String sql;
        AccountEntityDto ent = findClientAccounts(ClientMatricul).stream().findFirst().orElseThrow(() -> new ResourceNotFoundException("package_not_found"));
        if ( dao.getAccountId() == null) {
            sql = "select Top(?) cptHist.Devise,cptHist.representative,cptHist.LibAgence,cptHist.Agence,cptHist.Denomination,cptHist.Description, cptHist.Txnno, cptHist.serie, cpt.CpteJumelle, cpt.LibCatCpte as LibProduct,cptHist.Client,  cptHist.Credit,cptHist.Debit, cptHist.DateValeur, cptHist.DateOperation from CpteCltHist cptHist\r\n" + //
                    "inner join CpteClt cpt on cpt.CpteJumelle = cptHist.CpteJumelle and cpt.statut = 'Actif' " +//
                    "inner join product p on cpt.catcpte = p.productcode and (p.visdep='Yes' or p.VisRet='Yes') And Upper(Ltrim(Rtrim(p.ProductType))) In ('SAVING','CURRENT')" + //
                    "where  cptHist.Client = ?   " +//
                    "order by cptHist.serie desc ";


            List<ibnk.dto.BankingDto.AccountHistoryDto> result = new ArrayList<>();
            bankingJdbcTemplate.query(sql, new Object[]{dao.getCount(), ClientMatricul}, new int[]{java.sql.Types.INTEGER, java.sql.Types.VARCHAR}, (rs, rowNum) -> {
                ibnk.dto.BankingDto.AccountHistoryDto accountHistoryDto = new ibnk.dto.BankingDto.AccountHistoryDto();
                accountHistoryDto.setCurrency(ent.getCurrency());
                result.add(ibnk.dto.BankingDto.AccountHistoryDto.modelToDao(accountHistoryDto, rs, 0.0, 0.0));
                return null;
            });
            return result;
        } else {
            sql = "select Top(?) cptHist.Description,  cptHist.serie,  cpt.CpteJumelle,   cptHist.Credit, cptHist.Debit, cpt.LibCatCpte asy LibProduct, cptHist.Client, cptHist.DateValeur, cptHist.DateOperation from CpteCltHist cptHist\r\n" + //
                    "inner join CpteClt cpt on cpt.CpteJumelle = cptHist.CpteJumelle " +//
                    "inner join product p on cpt.catcpte = p.productcode and (p.visdep='Yes' or p.VisRet='Yes') And Upper(Ltrim(Rtrim(p.ProductType))) In ('SAVING','CURRENT')" + //
                    "where  cptHist.CpteJumelle = ? and cptHist.Client = ?  and cptHist.DateOperation BETWEEN ? and ?  " +//
                    "order by cptHist.serie desc ";

            List<ibnk.dto.BankingDto.AccountHistoryDto> result = new ArrayList<>();
            bankingJdbcTemplate.query(sql,
                    new Object[]{dao.getCount(), dao.getAccountId(), ClientMatricul, dao.getOpeningDate().toString(), dao.getClosingDate().toString()},
                    new int[]{java.sql.Types.INTEGER, java.sql.Types.VARCHAR, java.sql.Types.VARCHAR, java.sql.Types.VARCHAR, java.sql.Types.VARCHAR},
                    (rs, rowNum) -> {
                        ibnk.dto.BankingDto.AccountHistoryDto accountHistoryDto = new ibnk.dto.BankingDto.AccountHistoryDto();
                        accountHistoryDto.setCurrency(ent.getCurrency());
                        result.add(ibnk.dto.BankingDto.AccountHistoryDto.modelToDao(accountHistoryDto, rs, 0.0, 0.0));
                        return null;
                    });
            return result;
        }
    }


    //    public String blockCheckbook(CheckbookRequestDto checkbookRequestDto, Subscriptions subscriptions) throws SQLException, ResourceNotFoundException {
//        Connection connection = bankingRepository.getConnection();
//        Optional<BeneficiaryDto> beneficiaryInfo = checkClientBeneficiary(subscriptions.getClientMatricul(), beneficiaryDto.getBenefactorAccountNumber())
//                .stream()
//                .findFirst();
//        if(beneficiaryInfo.isEmpty()) {
//            throw new ResourceNotFoundException("account_not_found");
//        }
//        if(beneficiaryInfo.get().getId() > 0 ) {
//            throw new ResourceNotFoundException("beneficiary_exist");
//        }
//        String insertQuery = "INSERT INTO MobileBeneficiairy (" +
//                "[agence]\n" +
//                "\t\t\t\t   ,[client]\n" +
//                "\t\t\t\t   ,[donneur]\n" +
//                "\t\t\t\t   ,[beneficiaire]\n" +
//                "\t\t\t\t   ,[telephone]\n" +
//                "\t\t\t\t   ,[nom]\n" +
//                "\t\t\t\t   ,[onInstitution]\n" +
//                "\t\t\t\t   ) " +
//                "VALUES (?,?,?,?,?,?,?)";
//        PreparedStatement insertStmt = connection.prepareStatement(insertQuery);
//        insertStmt.setString(1, beneficiaryInfo.get().getAgence());
//        insertStmt.setString(2, subscriptions.getClientMatricul());
//        insertStmt.setString(3, subscriptions.getPrimaryAccount());
//        insertStmt.setString(4, beneficiaryDto.getBenefactorAccountNumber());
//        insertStmt.setString(5, beneficiaryInfo.get().getTelephone());
//        insertStmt.setString(6, beneficiaryInfo.get().getNom());
//        insertStmt.setString(7, "yes");
//        insertStmt.executeUpdate();
//        insertStmt.close();
//        return "Sucess";
//    }
//[MB_CHECKBOOK_REQUEST]
//    public void checkBookRequest(CheckbookRequestDto checkbookRequestDto, Subscriptions subscriptions) throws ResourceNotFoundException {
//        SimpleJdbcCall call = new SimpleJdbcCall(bankingJdbcTemplate).withProcedureName("MB_CHECKBOOK_REQUEST");
//        MapSqlParameterSource in = new MapSqlParameterSource()
//                .addValue("CpteJumelle", checkbookRequestDto.getAccountId())
//                .addValue("client", subscriptions.getClientMatricul())
//                .addValue("nombrePage", checkbookRequestDto.getNumberPages())
//                .addValue("description", checkbookRequestDto.getDescription());
//        Map<String, Object> out = checkBookRequest.execute(in);
//        if (!out.get("lect").equals(200)) throw new ResourceNotFoundException((String) out.get("ErrMsg"));
//        CheckbookRequestDto.modelToDao(out);
//    }
    @Transactional()
    public String checkbookRequest(CheckbookRequestDto checkbookRequestDto, Subscriptions subscriptions) throws ValidationException {
        Optional<Account> account = accountRepository.findByAccountNumber(checkbookRequestDto.getAccountId());
        if (account.isEmpty()) throw new ValidationException("account not found");
        if (!account.get().getClient().getClientId().equals(subscriptions.getClientMatricul()))
            throw new ValidationException("account not found");


        Account clientAccount = account.get();
        Long pendingRequest = checkBookRequestHistEntityRepository.countByCpteJumelleAndClientAndStatut(checkbookRequestDto.getAccountId().trim(), clientAccount.getClient().getClientId(), "PENDING");
        if (pendingRequest > 0) {
            throw new ValidationException("You already have a pending request");
        }

        CheckBookRequestHistEntity newRequest = new CheckBookRequestHistEntity();

        newRequest.setCpteJumelle(clientAccount.getAccountNumber());
        newRequest.setClient(clientAccount.getClient().getClientId());

        newRequest.setEmploye(clientAccount.getClient().getClientId()); // This value should be set accordingly

        newRequest.setDescription(checkbookRequestDto.getDescription());
        newRequest.setAgence(clientAccount.getBranchCode());
        newRequest.setDateCreation(Date.valueOf(LocalDate.now()));
        newRequest.setNombrepage(String.valueOf(checkbookRequestDto.getNumberPages()));
        newRequest.setStatut("PENDING");

        checkBookRequestHistEntityRepository.save(newRequest);

        return "successfull";
    }

    public List<CheckbookRequestDto> listChequeBook(Subscriptions subscriptions) {
        String sql = "SELECT  [id]\n" +
                "      ,[CpteJumelle]\n" +
                "      ,[Client]\n" +
                "      ,[Description]\n" +
                "      ,[DateCreation]\n" +
                "      ,[Nombrepage]\n" +
                "      ,[Statut]\n" +
                "      ,[RejectReason]\n" +
                "  FROM [Banking].[dbo].[CheckBookRequestHist] WHERE Client = ?";
        List<CheckbookRequestDto> result = new ArrayList<>();
        bankingJdbcTemplate.query(sql,
                new Object[]{subscriptions.getClientMatricul()},
                new int[]{java.sql.Types.VARCHAR},
                (rs, rowNum) -> {
                    CheckbookRequestDto checkbookRequestDto = new CheckbookRequestDto();

                    result.add(CheckbookRequestDto.modelToList(checkbookRequestDto, rs));
                    if (checkbookRequestDto.getEtat().trim().equalsIgnoreCase("SUSPEND") || checkbookRequestDto.getEtat().trim().equalsIgnoreCase("DELETED")) {
                        result.remove(checkbookRequestDto);
                    }
                    return null;
                });
        return result;
    }

    public List<CheckbookRequestDto.ChequeSeries> chequeSeriesByAccountId(String accountId, String clientId) {
        String sql = "SELECT  cheq.Dserie\n," +
                "	cheq.Fserie\n" +
                "  FROM [Banking].[dbo].[Chequier] cheq" +
                " JOIN CpteClt cmpcl ON cheq.Cptejumellecc = cmpcl.CpteJumelle " +
                "JOIN ClientBnk clt ON cmpcl.client = clt.client " +
                "WHERE cmpcl.CpteJumelle = ? AND clt.client= ?";
        List<CheckbookRequestDto.ChequeSeries> result = new ArrayList<>();
        bankingJdbcTemplate.query(sql,
                new Object[]{accountId, clientId},
                new int[]{java.sql.Types.VARCHAR, java.sql.Types.VARCHAR},
                (rs, rowNum) -> {
                    CheckbookRequestDto.ChequeSeries checklist = new CheckbookRequestDto.ChequeSeries();
                    result.add(CheckbookRequestDto.ChequeSeries.modelToList(checklist, rs));
                    return null;
                });
        return result;
    }


    public Tuple<Boolean, List<String>> findChequeBySeriesAndAccountId(CheckbookRequestDto.ChequeSeries series, String clientId) {
        boolean exist = true;
        List<String> checkSeries = new ArrayList<>();
        String sql = "SELECT cheq.Cptejumellecc, clt.NomJumelle, clt.LibAgence, clt.Agence,clt.cli" +
                " FROM [Banking].[dbo].[Chequier] cheq " +
                "JOIN [Banking].[dbo].[CpteClt] cmpcl ON cheq.Cptejumellecc = cmpcl.CpteJumelle " +
                "JOIN [Banking].[dbo].[ClientBnk] clt ON cmpcl.cli = clt.cli " +
                "WHERE ? >= Dserie AND ? <= Fserie AND Cptejumellecc = ? AND clt.cli = ?";

        for (String value : series.getChequeSeries()) {
            boolean chequeExists = Boolean.TRUE.equals(bankingJdbcTemplate.query(sql,
                    new Object[]{value, value, series.getAccountId(), clientId},
                    new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR},
                    (ResultSetExtractor<Boolean>) (rs) -> rs.next()));
            if (!chequeExists) {
                exist = false;
                checkSeries.add(value);
            }

        }
        return new Tuple<>(exist, checkSeries);
    }

    public Optional<String> findCheckUsed(CheckbookRequestDto.ChequeSeries series, String clientId) {
        Optional<String> used = Optional.empty();
        String sql = "SELECT \n" +
                "      trt.Tracer\n" +
                "      ,trt.DateCreation\n" +
                "      ,trt.Denomination\n" +
                "      ,trt.Serie\n" +
                "  FROM [Banking].[dbo].[CheqTrait] trt" +
                " JOIN [Banking].[dbo].[CpteClt] cmpclt ON trt.CptejumelleCC = cmpclt.CpteJumelle " +
                " JOIN [Banking].[dbo].[ClientBnk] clt ON cmpclt.client = clt.client " +
                "WHERE trt.Tracer = ? AND Cptejumellecc = ? AND clt.client= ?";
        for (String value : series.getChequeSeries()) {
            boolean chequeExists = Boolean.TRUE.equals(bankingJdbcTemplate.query(sql,
                    new Object[]{value, series.getAccountId(), clientId},
                    new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR},
                    (ResultSetExtractor<Boolean>) (rs) -> rs.next()));
            if (chequeExists) {
                used = Optional.of(value);
                break;
            }
        }
        return used;

    }

    public Optional<String> findCheckStopped(CheckbookRequestDto.ChequeSeries series, String clientId) {
        Optional<String> alreadyStopped = Optional.empty();
        String sql = " SELECT stp.CpteJumelle\n" +
                "  FROM [Banking].[dbo].[StopPayment] stp" +
                " JOIN [Banking].[dbo].[CpteClt] cmpclt ON stp.CpteJumelle = cmpclt.CpteJumelle " +
                " JOIN [Banking].[dbo].[ClientBnk] clt ON cmpclt.client = clt.client " +
                " WHERE stp.CpteJumelle = ? AND clt.client = ? AND stp.NumChequier = ?";
        for (String value : series.getChequeSeries()) {
            boolean chequeExists = Boolean.TRUE.equals(bankingJdbcTemplate.query(sql,
                    new Object[]{series.getAccountId(), clientId, value},
                    new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR},
                    (ResultSetExtractor<Boolean>) (rs) -> rs.next()));
            if (chequeExists) {
                alreadyStopped = Optional.of(value);
            }
        }
        return alreadyStopped;
    }

    public List<CheckbookRequestDto.StopPay> listCheckStoppedByAccountId(String accountId, String clientId) {
        String sql = "SELECT " +
                "      stp.CpteJumelle\n" +
                "           ,stp.NumChequier\n" +
                "           ,stp.DateEmission\n" +
                "           ,stp.Description\n" +
                "           ,stp.Denomination\n" +
                "           ,stp.DateOpposition\n" +
                ",stp.Agence\n" +
                ",stp.LibAgence" +
                "  FROM StopPayment stp" +
                " JOIN CpteClt cmpcl ON stp.CpteJumelle = cmpcl.CpteJumelle " +
                " JOIN ClientBnk clt ON cmpcl.client = clt.client " +
                "WHERE stp.CpteJumelle = ? AND clt.client= ?";
        List<CheckbookRequestDto.StopPay> result = new ArrayList<>();

        bankingJdbcTemplate.query(sql,
                new Object[]{accountId, clientId},
                new int[]{Types.VARCHAR, Types.VARCHAR},
                (rs, rowNum) -> {
                    CheckbookRequestDto.StopPay stoplist = new CheckbookRequestDto.StopPay();
                    result.add(CheckbookRequestDto.StopPay.modelToList(stoplist, rs));
                    return null;
                });
        return result;
    }

    public String stopPaymentInsert(CheckbookRequestDto.ChequeSeries series, String clientId) throws ResourceNotFoundException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        Tuple<Boolean, List<String>> exist = findChequeBySeriesAndAccountId(series, clientId);
        List<AccountEntityDto> account = findClientAccounts(series.getAccountId());
        if (exist.getFirst().equals(false)) {
            throw new ResourceNotFoundException("The following check(s) series don't exist" + exist.getSecond().stream().toList());
        } else {
            Optional<String> alreadyUsed = findCheckUsed(series, clientId);
            if (alreadyUsed.isPresent()) {
                throw new ResourceNotFoundException("Cheque Already Used" + alreadyUsed.get());
            }
            Optional<String> alreadyStopped = findCheckStopped(series, clientId);
            if (alreadyStopped.isPresent()) {
                throw new ResourceNotFoundException("Cheque Already Opposed" + alreadyStopped.get());
            }
            for (String value : series.getChequeSeries()) {
                String insertQuery = "INSERT INTO [dbo].[StopPayment]\n" +
                        "           ([CpteJumelle]\n" +
                        "           ,[NumChequier]\n" +
                        "           ,[DateEmission]\n" +
                        "           ,[Description]\n" +
                        "           ,[Denomination]\n" +
                        "           ,[Agence]\n" +
                        "           ,[LibAgence]\n" +
                        "           ,[DateOpposition]\n" +
                        "           ,[Employe]\n" +
                        "           ,[LibEmploye]\n" +
                        "           ,[Reference])\n" +
                        "     VALUES" +
                        "(?,?,?,?,?,?,?,?,?,?,?)";
                Object[] params = new Object[]{series.getAccountId(),
                        value,
                        LocalDate.now().format(formatter),
                        series.getDescription(),
                        account.get(0).getClientName(),
                        account.get(0).getOurBranchID(),
                        account.get(0).getBranchName(),
                        LocalDate.now().format(formatter),
                        "CLINT",
                        account.get(0).getClientName(),
                        value
                };
                int[] types = new int[]{java.sql.Types.VARCHAR, java.sql.Types.VARCHAR, java.sql.Types.VARCHAR, java.sql.Types.VARCHAR, java.sql.Types.VARCHAR,
                        java.sql.Types.VARCHAR, java.sql.Types.VARCHAR, java.sql.Types.VARCHAR, java.sql.Types.VARCHAR, java.sql.Types.VARCHAR, java.sql.Types.VARCHAR};
                bankingJdbcTemplate.update(insertQuery, params, types);
            }
            return "Success";
        }
    }

    @Transactional
    public String deleteCheckbookRequest(Long idTransaction, Subscriptions subscriptions) throws ValidationException {
        String errMsg;
        int lect;

        // Step 2: Check Request Existence
        CheckBookRequestHistEntity requestHist = checkBookRequestHistEntityRepository.findById(idTransaction).orElse(null);
        if (requestHist == null || !requestHist.getClient().equalsIgnoreCase(subscriptions.getClientMatricul().trim())) {
            errMsg = "Cannot delete this request";
            throw new ValidationException(errMsg);
        }

        if (requestHist.getStatut().trim().equalsIgnoreCase("PROCESS") || requestHist.getStatut().trim().equalsIgnoreCase("REJECTED")) {
            errMsg = "Cannot delete this request check status";
            throw new ValidationException(errMsg);
        }

        // Step 3: Update Request Status
        requestHist.setStatut("DELETED");
        checkBookRequestHistEntityRepository.save(requestHist);

        lect = 200;
        errMsg = "Request successfully deleted";
        return createResponse(lect, errMsg);
    }

    private String createResponse(int lect, String errMsg) {
        System.out.println("ErrMsg: " + errMsg);
        System.out.println("lect: " + lect);
        return errMsg;
    }

    public Optional<CheckbookRequestDto.StopPay> findStopPaymentByChecknumAndAccountId(String accountId, String checkNum, String clientId) {
        String sql = "SELECT " +
                " stp.CpteJumelle\n" +
                " ,stp.NumChequier\n" +
                " ,stp.Denomination\n" +
                " ,stp.Description\n" +
                " ,stp.Agence\n" +
                " ,stp.LibAgence\n" +
                " ,stp.Reference\n" +
                " ,stp.DateEmission\n" +
                " ,stp.DateOpposition\n" +
                "  FROM StopPayment stp" +
                " JOIN CpteClt cmpcl ON stp.CpteJumelle = cmpcl.CpteJumelle " +
                " JOIN ClientBnk clt ON cmpcl.client = clt.client " +
                "WHERE stp.CpteJumelle = ? AND clt.cli= ? AND stp.NumChequier = ?";
        CheckbookRequestDto.StopPay stopPay = new CheckbookRequestDto.StopPay();
        return bankingJdbcTemplate.query(sql, new Object[]{accountId, clientId, checkNum},
                new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR},
                rs -> {
                    if (rs.next()) {
                        CheckbookRequestDto.StopPay.modelToList(stopPay, rs);
                        return Optional.of(stopPay);
                    }
                    return Optional.empty();
                });
    }

    @Transactional
    public String OppliftStopPayment(CheckbookRequestDto.StopPay stop, String clientId) throws ValidationException {
        Optional<CheckbookRequestDto.StopPay> stopPay = findStopPaymentByChecknumAndAccountId(stop.getAccount(), stop.getCheckNumber(), clientId);
        if (stopPay.isEmpty()) {
            throw new ValidationException("Check Stopped Does not Exist");
        }
        StopPaymentHist stopPay1 = StopPaymentHist.builder()
                .agence(stopPay.get().getAgence())
                .libAgence(stopPay.get().getLibAgence())
                .checkNum(stopPay.get().getCheckNumber())
                .accountId(stopPay.get().getAccount())
                .description(stopPay.get().getReason())
                .employe(stopPay.get().getEmploye())
                .libEmploye(stopPay.get().getLibEmploye())
                .reference(stopPay.get().getReference())
                .oppositionDate(stopPay.get().getOppositionDate()).build();
        stopPaymentHistRepository.save(stopPay1);
        String deleteQuery = "DELETE FROM StopPayment WHERE Numchequier = ? AND CpteJumelle = ?";
        int rowsAffected = bankingJdbcTemplate.update(deleteQuery, stop.getCheckNumber(), stop.getAccount());

        if (rowsAffected > 0) {
            return "Delete successful";
        }
        return "Delete Failed";

    }
}
