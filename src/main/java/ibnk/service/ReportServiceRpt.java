package ibnk.service;

import com.crystaldecisions.sdk.occa.report.lib.ReportSDKExceptionBase;
import ibnk.dto.AccountHistoryDto;
import ibnk.dto.BankingDto.AccountEntityDto;
import ibnk.dto.BankingDto.AccountHistoryRes;
import ibnk.dto.UserDto;
import ibnk.models.banking.InstitutionEntity;
import ibnk.models.internet.NotificationTemplate;
import ibnk.models.internet.client.Subscriptions;
import ibnk.models.internet.enums.EventCode;
import ibnk.models.internet.enums.NotificationChanel;
import ibnk.models.rptBanking.PrintStmtEntity;
import ibnk.models.rptBanking.RptAcctOpenInfoEntity;
import ibnk.repositories.internet.NotificationTemplateRepository;
import ibnk.repositories.rptBanking.PrintstmtRepository;
import ibnk.repositories.rptBanking.RptAcctOpenInfoRepository;
import ibnk.service.BankingService.AccountService;
import ibnk.tools.error.ResourceNotFoundException;
import ibnk.intergrations.EmailService;
import ibnk.security.PasswordConstraintValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ibnk.service.OtpService.replaceParameters;

@Service
@RequiredArgsConstructor
public class ReportServiceRpt {
    private final MediaService mediaService;
    private final AccountService accountService;
    private final EmailService emailService;
    private final InstitutionConfigService institutionConfigService;
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final PrintstmtRepository printstmtRepository;

//    @Qualifier("rptBankingJdbcTemplate")
//    private final JdbcTemplate bankingJdbcTemplate;
//    private final InstitutionRepository institutionRepository;
    private final RptAcctOpenInfoRepository rptAcctOpenInfoRepository;


    @Transactional
    public byte[] clientReportStatment(AccountHistoryDto dao, Subscriptions sub, String ExportType) throws ResourceNotFoundException, ReportSDKExceptionBase, IOException {
        printstmtRepository.deleteAllByEmployeeId(sub.getUuid());
        AccountHistoryRes statement = accountService.clientAccountHistory(dao, sub);

        List<PrintStmtEntity> prtlist = new ArrayList<>();

        for (ibnk.dto.BankingDto.AccountHistoryDto accountHistoryDto : statement.getHistoryDto()) {
            PrintStmtEntity prt = new PrintStmtEntity();
            prt.setAgence(accountHistoryDto.getAgence());
            prt.setId(Long.parseLong(accountHistoryDto.getIndex().toString()));
            prt.setDevise(statement.getCurrency());
            prt.setLibDevise(accountHistoryDto.getCurrency());
            prt.setLibAgence(accountHistoryDto.getLibAgence());
            prt.setAcctName(accountHistoryDto.getDenomination());
            prt.setEmployeeId(sub.getUuid());
            prt.setAcctNo(accountHistoryDto.getClient()+ " " + accountHistoryDto.getAccountName() + " " + accountHistoryDto.getAccountID());
            prt.setMtCredit(accountHistoryDto.getCredit());
            prt.setMtDebit(accountHistoryDto.getDebit());
            prt.setRepDebit(0.0);
            prt.setRepCredit(accountHistoryDto.getOpeningBalance());
            prt.setDateOperation(accountHistoryDto.getOperationDate().substring(0, 10));
            prt.setDescription(accountHistoryDto.getDescription());
            prt.setSens(accountHistoryDto.getCredit() > 0 ? "Cr" : "Db");
            prt.setRefOperation(accountHistoryDto.getTxnno());
            prt.setBeginDate(Date.valueOf(dao.getOpeningDate().toString()));
            prt.setEndDate(Date.valueOf(dao.getClosingDate().toString()));
            prt.setSolde(accountHistoryDto.getClosingBalance());
            prt.setSerieNum(accountHistoryDto.getIndex());
            prtlist.add(prt);
        }

        printstmtRepository.saveAll(prtlist);
        InstitutionEntity data = mediaService.logoSwitch();
        return mediaService.stmtReport(data.getInstitution(),data.getPoBox(), "ACCOUNTING STATEMENT/RELEVE DE COMPTE", data.getTele(), data.getTown(), sub.getUuid(), ExportType);
    }
//    TODO RptLogo
//    InstitutionEntity insGlobal = institutionRepository.findAll().stream().findFirst().orElseThrow(()->new ResourceNotFoundException("An Error Occured Institution logo"));
//    //        RptLogoEntity logo = rptLogoRepository.findAll().stream().findFirst().orElseThrow(()->new ResourceNotFoundException("An Error Occured"));
////        logo.setLogo(insGlobal.getLogo());
//    Object[] params = null;
//
//    String insertQuery = "INSERT INTO rptLogo ([logo]) VALUES (?)";
//    params = new Object[]{
//        insGlobal.getLogo()};
//    int[] types = new int[]{Types.BLOB};
//        bankingJdbcTemplate.update(insertQuery, params, types);
    @Transactional
    public String eClientReportStatement(AccountHistoryDto dao, Subscriptions sub, String ExportType) throws ResourceNotFoundException, ReportSDKExceptionBase, IOException {
        String Message = "";
        String Subject = "";
        Optional<NotificationTemplate> notificationTemplate = notificationTemplateRepository.findByNotificationTypeAndEventCode(NotificationChanel.MAIL, String.valueOf(EventCode.E_ACCOUNT_STATEMENT));
        List<Object> payloads = new ArrayList<>();
        payloads.add(UserDto.CreateSubscriberClientDto.modelToDao(sub));
        if (!sub.getEmail().isEmpty()) {
            if (!PasswordConstraintValidator.isValidEmail(sub.getEmail())) {
                throw new ResourceNotFoundException("Invalid Email Address");
            }
        }
        if (notificationTemplate.isPresent()) {
            if (notificationTemplate.get().getStatus().equals("ACTIVE")) {
                Message = replaceParameters(notificationTemplate.get().getTemplate(), payloads);
                Subject = replaceParameters(notificationTemplate.get().getSubject(), payloads);
            }

        }
        byte[] attachment = clientReportStatment(dao, sub, ExportType);
        if (notificationTemplate.get().getNotificationType().equals(NotificationChanel.MAIL)) {
            emailService.sendSimpleMessageAttach(sub.getEmail(), Subject, Message, dao.getAccountId(), attachment);
        }
        return "Success";

    }

    @Transactional
    public byte[] rptAcctOpenInfo(String accountNumber, String ExportType) throws ResourceNotFoundException, ReportSDKExceptionBase, IOException {
        rptAcctOpenInfoRepository.deleteAllByCpteJumelle(accountNumber);
        AccountEntityDto account = accountService.findClientAccounts(accountNumber).stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("account_not_found"));
        RptAcctOpenInfoEntity rptAcctOpenInfoEntity = new RptAcctOpenInfoEntity();
        rptAcctOpenInfoEntity.setAgence(account.getOurBranchID());
        rptAcctOpenInfoEntity.setClient(account.getClient());
        rptAcctOpenInfoEntity.setCpteJumelle(account.getAccountID());
        rptAcctOpenInfoEntity.setLibClient(account.getClientName());
        rptAcctOpenInfoEntity.setDateOuverture(account.getDateCreation());
        rptAcctOpenInfoEntity.setAddress1(account.getAdresse1());
        rptAcctOpenInfoEntity.setTelephone1(account.getPhone1());
        rptAcctOpenInfoEntity.setAddress2(account.getAdresse1());
        rptAcctOpenInfoEntity.setLibAgence(account.getBranchName());
        rptAcctOpenInfoEntity.setLibGestionnaire(account.getClientManager());
        rptAcctOpenInfoEntity.setTypeClient(account.getClientType());
        rptAcctOpenInfoEntity.setCatClient(account.getProductType());
        rptAcctOpenInfoEntity.setDateNaissance(account.getDateOfBirth());
        rptAcctOpenInfoEntity.setLieuNaissance(account.getPlaceOfBirth());
        rptAcctOpenInfoEntity.setCniPass(account.getIdentificationType());
        rptAcctOpenInfoEntity.setLibNationalite(account.getLibNationalite());
        rptAcctOpenInfoEntity.setBp(account.getPoBox());
        rptAcctOpenInfoEntity.setLibville(account.getTown());
        rptAcctOpenInfoEntity.setProfession(account.getProfession());
        rptAcctOpenInfoEntity.setNomPere(account.getFatherName());
        rptAcctOpenInfoEntity.setNomMere(account.getMotherName());
        rptAcctOpenInfoEntity.setTelContact(account.getPContactPhoneNumber());
//        rptAcctOpenInfoEntity.setNomContact();
//        rptAcctOpenInfoEntity.setAdresseContact(account.getPContactAddress());
        rptAcctOpenInfoEntity.setDteDelCni(account.getIdentificationIssueDate());
        rptAcctOpenInfoEntity.setDteExpCni(account.getIdentificationExpireDate());
        rptAcctOpenInfoEntity.setTelephone2(account.getPhoneNumber2());
        rptAcctOpenInfoEntity.setLieuCni(account.getPlaceIssueIdentification());
        rptAcctOpenInfoRepository.save(rptAcctOpenInfoEntity);
        return mediaService.rptacctopeninfoMoReport(accountNumber, ExportType);

    }
}
