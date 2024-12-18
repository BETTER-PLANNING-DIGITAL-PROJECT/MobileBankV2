package ibnk.tools;

import ibnk.models.internet.*;
import ibnk.models.internet.enums.*;
import ibnk.repositories.internet.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Component
public class SetupDataLoader implements ApplicationListener<ContextRefreshedEvent> {
    boolean alreadySetup = false;
    private final UserRepository userRepository;
    private final InstitutionConfigRepository institutionConfigRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final TransactionStatusMessageRepository transactionStatusMessageRepository;

    private final TermAndConditionRepository termAndConditionRepository;
    @Value("${betta.sms.sender.id}")
    String senderId;

    public SetupDataLoader(UserRepository userRepository, InstitutionConfigRepository institutionConfigRepository, PasswordEncoder passwordEncoder, TransactionStatusMessageRepository transactionStatusMessageRepository, NotificationTemplateRepository notificationTemplateRepository, TermAndConditionRepository termAndConditionRepository) {
        this.userRepository = userRepository;
        this.institutionConfigRepository = institutionConfigRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationTemplateRepository = notificationTemplateRepository;
        this.termAndConditionRepository = termAndConditionRepository;
        this.transactionStatusMessageRepository = transactionStatusMessageRepository;
    }

    @Override
    @Transactional("primaryTransactionManagerFactory")
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (alreadySetup) {
            return;
        }
        List<UserEntity> users = new ArrayList<>();
        UserEntity admin1 = UserEntity.builder()
                .id(1L)
                .uuid("5dde1e2b-3543-47b3-a48e-d4e512b5d056")
                .name("Supper Admin I")
                .passExpiration(false)
                .doubleAuthentication(false)
                .firstLogin(false)
                .password(passwordEncoder.encode("11112222"))
                .userLogin("10000")
                .build();

        UserEntity admin2 = UserEntity.builder()
                .id(3L)
                .uuid("36es874se-3543-47b3-a68e-d4e512f5d056")
                .name("Supper Admin II")
                .passExpiration(false)
                .doubleAuthentication(false)
                .firstLogin(false)
                .password(passwordEncoder.encode("11112222"))
                .userLogin("20000")
                .build();

        Optional<UserEntity> user1 = userRepository.findByUserLogin("10000");
        Optional<UserEntity> user2 = userRepository.findByUserLogin("20000");
        if(user1.isEmpty()) {
            users.add(admin1);
        }

        if(user2.isEmpty()) {
            users.add(admin2);
        }

        if(!users.isEmpty()) {
            userRepository.saveAll(users);
        }

//        Optional<InstitutionConfig> inst = institutionConfigRepository.findById(1L);

//            InstitutionConfig institutionConfig = InstitutionConfig.builder()
//                    .id(6L)
//                    .institutionName("BETTER PLANNING LTD")
//                    .institutionEmail("noreply@finasddee-creditline.com")
//                    .emailPassword("Finas@@2024*#")
//                    .host("mail.finasddee-creditline.com")
//                    .Phone("")
//                    .POBOX("3755")
//                    .Town("Douala-Bali")
//                    .subMethod(SubMethod.AUTOMATIC)
//                    .defaultPackage("STD")
//                    .emailNoReply("noreply@finasddee-creditline.com")
//                    .port(465L)
//                    .questConfig(QuestionEnum.AUTO)
//                    .returnUrl("https://internetbanking.finasddee-creditline.com/auth/diaspora-deposit-response?i=")
//                    .maxSecurityQuest(5L)
//                    .minSecurityQuest(2L)
//                    .verifyQuestNumber(1)
//                    .verificationResetTimer(15)
//                    .maxVerifyAttempt(6L)
//                    .otpMinBeforeExpire(5L)
//                    .application(Application.IBNK.name())
//                    .trnasOtp(true)
//                    .build();
        InstitutionConfig institutionConfig2 = InstitutionConfig.builder()
                .institutionName("BETTER PLANNING LTD")
                .institutionEmail("webmaster@betterplanning.net")
                .emailPassword("R7,1$KbKKUdIm!$=bD")
                .host("mail.betterplanning.net")
                .Phone("677415083")
                .POBOX("4022")
                .Town("Douala")
                .subMethod(SubMethod.AUTOMATIC)
                .defaultPackage("001")
                .emailNoReply("benzeezmokom@gmail.com")
                .port(465L)
                .questConfig(QuestionEnum.AUTO)
                .returnUrl("internetbanking.finasddee-creditline.com/auth/diaspora-deposit-response?i=")
                .maxSecurityQuest(2L)
                .minSecurityQuest(0L)
                .verifyQuestNumber(1)
                .verificationResetTimer(15)
                .maxVerifyAttempt(6L)
                .otpMinBeforeExpire(5L)
                .application(Application.MB.name())
                .PayerFeePercentage(0)
                .trnasOtp(true)
                .build();

       saveInstitutionConfig(institutionConfig2);


        insertEventCodesEnumValues();
        insertClientEventValues();
        insertTermsAndConditions();
        insertTypeOperationAndStatusValues();
        alreadySetup = true;
    }

    private void saveInstitutionConfig(InstitutionConfig institutionConfig) {
        Optional<InstitutionConfig> existingConfig = institutionConfigRepository.findByApplication(institutionConfig.getApplication());
        if (existingConfig.isEmpty()) {
            institutionConfigRepository.save(institutionConfig);
        }
    }

    public void insertClientEventValues() {
        for (NotificationCode eventCode : NotificationCode.values()) {
            saveNotificationCode(String.valueOf(eventCode));
        }
    }

    public void insertTypeOperationAndStatusValues() {
        for (TypeOperations eventCode : TypeOperations.values()) {
            saveTransactionMessage(String.valueOf(eventCode), Status.CANCELLED.toString());
            saveTransactionMessage(String.valueOf(eventCode), Status.FAILED.toString());
            saveTransactionMessage(String.valueOf(eventCode), Status.SUCCESS.toString());
        }
    }

    public void insertEventCodesEnumValues() {
        for (EventCode eventCode : EventCode.values()) {
            saveNotificationCode(String.valueOf(eventCode));
        }
    }

    public void insertTermsAndConditions() {
        for (TermConditionCodes code : TermConditionCodes.values()) {
            saveTermsAndConditions(String.valueOf(code));
        }
    }

    private void saveNotificationCode(String s) {
        Optional<NotificationTemplate> smsEvent = notificationTemplateRepository.findByNotificationTypeAndEventCode(NotificationChanel.SMS, s);
        if (smsEvent.isEmpty()) {
            NotificationTemplate newSmsEvent = NotificationTemplate.builder()
                    .eventCode(s)
                    .subject(senderId)
                    .status("INACTIVE")
                    .template(senderId + "")
                    .notificationType(NotificationChanel.SMS).build();
            notificationTemplateRepository.save(newSmsEvent);
        }
        Optional<NotificationTemplate> mailEvent = notificationTemplateRepository.findByNotificationTypeAndEventCode(NotificationChanel.MAIL, s);
        if (mailEvent.isEmpty()) {
            NotificationTemplate newSmsEvent = NotificationTemplate.builder()
                    .eventCode(s)
                    .status("INACTIVE")
                    .template("")
                    .notificationType(NotificationChanel.MAIL).build();
            notificationTemplateRepository.save(newSmsEvent);
        }
    }

    private void saveTransactionMessage(String type, String status) {
        Optional<TransactionStatusMessage> message = transactionStatusMessageRepository.findTransactionStatusMessageByTypeOpAndStatus(TypeOperations.valueOf(type), Status.valueOf(status));
        if (message.isEmpty()) {
            TransactionStatusMessage newMessage = TransactionStatusMessage.builder()
                    .status(Status.valueOf(status))
                    .typeOp(TypeOperations.valueOf(type)).build();
            transactionStatusMessageRepository.save(newMessage);
        }

    }

    private void saveTermsAndConditions(String code) {
        Optional<TermAndCondition> term = termAndConditionRepository.findByCode(code);
        if (term.isEmpty()) {
            TermAndCondition model = TermAndCondition.builder()
                    .code(code)
                    .text("").build();
            termAndConditionRepository.save(model);
        }
    }
}
