package ibnk.service;

import ibnk.dto.BankingDto.AccountEntityDto;
import ibnk.dto.BankingDto.ClientQuestDto;
import ibnk.dto.auth.CustomerVerification;
import ibnk.models.internet.*;
import ibnk.models.internet.client.ClientConfig;
import ibnk.models.internet.client.ClientSecurityQuestion;
import ibnk.models.internet.client.Subscriptions;
import ibnk.models.internet.enums.NotificationChanel;
import ibnk.models.internet.enums.Status;
import ibnk.models.internet.enums.SubscriberStatus;
import ibnk.models.internet.enums.VerificationType;
import ibnk.repositories.internet.*;
import ibnk.service.BankingService.AccountService;
import ibnk.tools.error.FailedSecurityVerification;
import ibnk.tools.error.ResourceNotFoundException;
import ibnk.tools.error.UnauthorizedUserException;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class InstitutionConfigService {

    private final InstitutionConfigRepository institutionConfigRepository;
    private final TransactionStatusMessageRepository transactionStatusMessageRepository;
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClientConfigRepository clientConfigRepository;
    private final ClientVerificationRepository clientVerificationRepository;
    private final ClientVerificationArchiveRepository clientVerificationArchiveRepository;
    private final ClientSecurityQuestionRepository clientSecurityQuestionRepository;
    private final AccountService accountService;
    private final EmailServerRepository emailServerRepository;
    private final SubscriptionRepository subscriptionRepository;

    private final TermAndConditionRepository termAndConditionRepository;

    public String saveClientConfig(ClientConfig clientConfig, Subscriptions subs) throws  ResourceNotFoundException {
        AccountEntityDto account = accountService.findClientAccounts(clientConfig.getAccountNumber()).stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("account_not_found"));
        if (!account.getClientID().equals(subs.getClientMatricul())) {
            throw new ResourceNotFoundException("account_not_found");
        }
        clientConfig.setClient(account.getClient());
        clientConfig.setAccountName(account.getAccountName());
        clientConfig.setAccountNumber(account.getAccountID());
        clientConfigRepository.save(clientConfig);
//
        return "Account Not Found";
    }

    public String updateClientConfig(ClientConfig dto, Subscriptions subs) throws ResourceNotFoundException {
        ClientConfig clientConfig = clientConfigRepository.findByIdAndSubscriptions(dto.getId(), subs)
                .orElseThrow(() -> new ResourceNotFoundException("Config Does not Exist"));

        AccountEntityDto account = accountService.findClientAccounts(clientConfig.getAccountNumber()).stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("account_not_found"));

        clientConfig.setTransLimit(dto.getTransLimit());
        clientConfig.setAccountName(account.getAccountName());
        clientConfig.setMinAmount(dto.getMinAmount());
        clientConfig.setMaxAmount(dto.getMaxAmount());
        clientConfig.setAccountNumber(account.getAccountID());
        clientConfig.setAlertAmount(dto.getAlertAmount());
        clientConfig.setAlertTrans(dto.getAlertTrans());
        clientConfig.setClient(subs.getClientMatricul());

        clientConfigRepository.save(clientConfig);

        return "Account Not Found";
    }

    public List<ClientConfig> listClientConfig(Subscriptions subscriptions) {
        List<AccountEntityDto> clientAccounts = accountService.findClientAccounts(subscriptions.getClientMatricul());
        List<ClientConfig> clientConfigs = new ArrayList<>(clientConfigRepository.findClientConfigBySubscriptions(subscriptions).stream().toList());
        clientAccounts.forEach((accountEntityDto -> {
            boolean exist = clientConfigs.stream().anyMatch(clientConfig -> clientConfig.getAccountNumber().equals(accountEntityDto.getAccountID()));
            if (!exist) {
                ClientConfig object = ClientConfig.builder()
                        .client(accountEntityDto.getClient())
                        .accountNumber(accountEntityDto.getAccountID())
                        .accountName(accountEntityDto.getAccountName())
                        .alertAmount(false)
                        .alertTrans(false)
                        .build();
                clientConfigs.add(object);
            }
        }));
        return clientConfigs;
    }

    public ClientConfig getClientConfigById(Long id, Subscriptions sub) {
        return clientConfigRepository.findById(id).get();
    }

    public String updateInstitutionConfig(InstitutionConfig institutionConfig) throws ResourceNotFoundException {
        InstitutionConfig institutionConfig1 = institutionConfigRepository.findById(institutionConfig.getId())
                .orElseThrow(() -> new ValidationException("Config Does not Exist"));
        institutionConfig1.setQuestConfig(institutionConfig.getQuestConfig());
        institutionConfig1.setMaxSecurityQuest(institutionConfig.getMaxSecurityQuest());
        institutionConfig1.setMinSecurityQuest(institutionConfig.getMinSecurityQuest());
        institutionConfig1.setVerifyQuestNumber(institutionConfig.getVerifyQuestNumber());
        institutionConfig1.setInstitutionName(institutionConfig.getInstitutionName());
        institutionConfig1.setOtpMinBeforeExpire(institutionConfig.getOtpMinBeforeExpire());
        institutionConfig1.setVerificationResetTimer(institutionConfig.getVerificationResetTimer());
        institutionConfig1.setReturnUrl(institutionConfig.getReturnUrl());
        institutionConfig1.setMaxVerifyAttempt(institutionConfig.getMaxVerifyAttempt());
        institutionConfig1.setInstitutionEmail(institutionConfig.getInstitutionEmail());
        institutionConfig1.setEmailPassword(institutionConfig.getEmailPassword());
        institutionConfig1.setHost(institutionConfig.getHost());
        institutionConfig1.setServer(institutionConfig.getServer());
        institutionConfig1.setDefaultPackage(institutionConfig.getDefaultPackage());
        institutionConfig1.setInstitutionShortName(institutionConfig.getInstitutionShortName());
        institutionConfig1.setEmailNoReply(institutionConfig.getEmailNoReply());
        institutionConfig1.setSubMethod(institutionConfig.getSubMethod());
        institutionConfig1.setTown(institutionConfig.getTown());
        institutionConfig1.setPOBOX(institutionConfig.getPOBOX());
        institutionConfig1.setPhone(institutionConfig.getPhone());
        institutionConfig1.setPort(institutionConfig.getPort());
//        EmailServer serve = emailServerRepository.findById(institutionConfig.getServerId()).orElseThrow(() -> new ResourceNotFoundException("Server Does not Exist"));
//        institutionConfig1.setServer(serve);
        institutionConfigRepository.save(institutionConfig1);
        return "Updated";
    }

    public String saveServer(EmailServer server) throws ResourceNotFoundException {
        Optional<EmailServer> host = emailServerRepository.findEmailServerByHost(server.getHost());
        Optional<EmailServer> port = emailServerRepository.findEmailServerByPort(server.getPort());
        Optional<EmailServer> email = emailServerRepository.findEmailServerByInstitutionEmail(server.getInstitutionEmail());
        if (host.isPresent() || port.isPresent() || email.isPresent()) {
            throw new ResourceNotFoundException("Host or Port or Email Already Exist");
        }
        String srt = passwordEncoder.encode(server.getEmailPassword());
        server.setEmailPassword(srt);
        emailServerRepository.save(server);
        return "Saved";
    }

    public String updateServer(Long id, EmailServer server) throws ResourceNotFoundException {
        EmailServer update = emailServerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Server does Not Exist"));
        update.setHost(server.getHost());
        update.setPort(server.getPort());
        update.setEmailNoReply(server.getEmailNoReply());
        update.setInstitutionEmail(server.getInstitutionEmail());
        update.setEmailPassword(server.getEmailPassword());
        String srt = passwordEncoder.encode(update.getEmailPassword());
        update.setEmailPassword(srt);

        emailServerRepository.save(update);
        return "Updated";
    }

    public String DeleteEmailServer(Long id) {
        emailServerRepository.findById(id).orElseThrow(() -> new ResourceAccessException("Does not Exist"));
        emailServerRepository.deleteById(id);
        return "Deleted";
    }

    public List<EmailServer> listEmailServer() {
        return emailServerRepository.findAll();
    }

    public InstitutionConfig getInstConfig() throws ValidationException {
        return institutionConfigRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new ValidationException("Empty"));

    }
    public InstitutionConfig findByyApp(String app) throws ValidationException {
        return institutionConfigRepository.findByApplication(app)
                .orElseThrow(() -> new ValidationException("Empty"));

    }


    public NotificationTemplate findById(Long id) throws ResourceNotFoundException {
        return notificationTemplateRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Not Found"));
    }

    public String UpdateNotifTemplate(Long id, NotificationTemplate dto) throws ResourceNotFoundException {
        NotificationTemplate notificationTemplate = findById(id);
        notificationTemplate.setTemplate(dto.getTemplate());
        notificationTemplate.setSubject(dto.getSubject());
        notificationTemplate.setStatus(dto.getStatus());
        notificationTemplateRepository.save(notificationTemplate);
        return "Success";
    }

    public NotificationTemplate findByNotificationTypeAndEventCode(String notification, String eventCode) throws ResourceNotFoundException {
        return notificationTemplateRepository.findByNotificationTypeAndEventCode(NotificationChanel.valueOf(notification), eventCode)
                .orElseThrow(() -> new ResourceNotFoundException("Not Exist"));
    }

    public String deleteNotifById(Long id) throws ResourceNotFoundException {
        NotificationTemplate del = findById(id);
        notificationTemplateRepository.deleteById(id);
        return "Deleted successfully: " + del;
    }

    public List<NotificationTemplate> listNotifTemplate() {
        return notificationTemplateRepository.findAll();
    }


    public TransactionStatusMessage findTransactionMessageByType(String type) throws ResourceNotFoundException {
        return transactionStatusMessageRepository.findByTypeOp(type).orElseThrow(() -> new ResourceNotFoundException("Not Found"));
    }

    public TransactionStatusMessage findTransactionMessageById(Long id) throws ResourceNotFoundException {
        return transactionStatusMessageRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Not Found"));
    }

    public String UpdateTransactionMessageById(Long id, TransactionStatusMessage dto) throws ResourceNotFoundException {
        TransactionStatusMessage transactionStatusMessage = findTransactionMessageById(id);
        transactionStatusMessage.setText(dto.getText());
        transactionStatusMessageRepository.save(transactionStatusMessage);
        return "Success";
    }

    public List<TransactionStatusMessage> listTransactionMessage() {
        return transactionStatusMessageRepository.findAll();
    }

    public CustomerVerification countRemainingCustomerTrials(Subscriptions sub, InstitutionConfig config, VerificationType type) throws  UnauthorizedUserException {
        LocalDateTime time = LocalDateTime.now().minusMinutes(config.getVerificationResetTimer());
        Integer previousTrials = clientVerificationRepository.countPreviousFailedTrials(sub, Status.FAILED, time, type);
        Integer leftTrials = Math.toIntExact(config.getMaxVerifyAttempt() - previousTrials);
        CustomerVerification responseData = new CustomerVerification();

        responseData.setTrials(leftTrials);
        responseData.setMaxTrials(Math.toIntExact(config.getMaxVerifyAttempt()));
        if (leftTrials.compareTo(1) < 0) {
            sub.setStatus(SubscriberStatus.SUSPENDED.name());
            subscriptionRepository.save(sub);
            throw new UnauthorizedUserException("account_suspended");
        };
        return responseData;
    }

    public void archiveClientVerifications(Subscriptions sub, VerificationType type) {
        List<ClientVerification> verifications = clientVerificationRepository.findClientVerificationBySubscriptionsAndVerificationType(sub, type);
        List<ClientVerificationArchive> archives = new ArrayList<>();

        for (ClientVerification verification : verifications) {
            ClientVerificationArchive archive = new ClientVerificationArchive();

            archive.setId(verification.getId());
            archive.setUuid(verification.getUuid());
            archive.setRole(verification.getRole());
            archive.setVerificationType(verification.getVerificationType());
            archive.setVerified(verification.isVerified());
            archive.setSubscriptions(sub);
            archive.setIp(verification.getIp());
            archive.setPhoneNumber(verification.getPhoneNumber());
            archive.setMessage(verification.getMessage());
            archive.setStatus(verification.getStatus());

            archives.add(archive);
        }
        clientVerificationArchiveRepository.saveAll(archives);
        clientVerificationRepository.deleteAllInBatch(verifications);
    }

    public ClientVerification verifySecurityQuestions(List<ClientQuestDto> json, Subscriptions subs) throws ResourceNotFoundException {
        InstitutionConfig config = getInstConfig();
        if (config.getVerifyQuestNumber() != json.size()) {
            throw new FailedSecurityVerification("Insufficient Questions for this Process", null);
        }
        for (ClientQuestDto QuestDto : json) {
            Optional<ClientSecurityQuestion> clientQuestion = clientSecurityQuestionRepository.findByClientIdAndQuestionId(subs.getId(), QuestDto.getSecurityQuestionId());
            if (clientQuestion.isEmpty()) {
                throw new FailedSecurityVerification("question-not-set", null);
            }
            boolean answerMatch = passwordEncoder.matches(QuestDto.getSecurityAns().toLowerCase(), clientQuestion.get().getSecurityAns());
            if (!answerMatch) {
                throw new FailedSecurityVerification("wrong-answer", null);
            }

        }
        ClientVerification verify = ClientVerification.builder()
                .subscriptions(subs)
                .status(Status.SUCCESS)
                .verified(true)
                .verificationType(VerificationType.SECURITY_QUESTION)
                .build();
        clientVerificationRepository.save(verify);
        archiveClientVerifications(subs, VerificationType.SECURITY_QUESTION);
        return verify;
    }

    public String SaveTerm(TermAndCondition dto) throws ResourceNotFoundException {
        if (findTermAndConditionByCode(dto.getCode()).getCode().equalsIgnoreCase(dto.getCode()))
            throw new ResourceNotFoundException("Code Already Exist");
        termAndConditionRepository.save(dto);
        return "Saved";
    }

    public String UpdateTerm(TermAndCondition dto) throws ResourceNotFoundException {
        TermAndCondition term = findTermAndConditionByCode(dto.getCode());
        term.setText(dto.getText());
        term.setCode(dto.getCode());
        termAndConditionRepository.save(term);
        return "Updated";
    }

    public List<TermAndCondition> ListTerm() {
        return termAndConditionRepository.findAll();
    }

    public String DeleteTerm(long id) {
        termAndConditionRepository.deleteById(id);
        return "Deleted";
    }

    public TermAndCondition findTermAndConditionByCode(String code) throws ResourceNotFoundException {
        return termAndConditionRepository.findByCode(code).orElseThrow(() -> new ResourceNotFoundException("Term Does Not Exist"));
    }

}
