package ibnk.service;

import ibnk.dto.BankingDto.TransferModel.MobilePayment;
import ibnk.dto.auth.OtpAuth;
import ibnk.dto.auth.CustomerVerification;
import ibnk.intergrations.BetaSms.BetaSmsService;
import ibnk.intergrations.BetaSms.ResponseDto.BetaResponse;
import ibnk.models.internet.ClientVerification;
import ibnk.models.internet.InstitutionConfig;
import ibnk.models.internet.NotificationTemplate;
import ibnk.models.internet.OtpEntity;
import ibnk.models.internet.client.ClientRequest;
import ibnk.models.internet.client.Subscriptions;
import ibnk.models.internet.enums.*;
import ibnk.repositories.internet.ClientRequestRepository;
import ibnk.repositories.internet.ClientVerificationRepository;
import ibnk.repositories.internet.NotificationTemplateRepository;
import ibnk.repositories.internet.OtpRepository;
import ibnk.service.BankingService.MobilePaymentService;
import ibnk.tools.error.ExpiredPasswordException;
import ibnk.tools.error.FailedSecurityVerification;
import ibnk.tools.error.ResourceNotFoundException;
import ibnk.tools.error.UnauthorizedUserException;
import ibnk.tools.nexaConfig.EmailService;
import ibnk.tools.nexaConfig.NexaService;
import ibnk.tools.security.PasswordConstraintValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;


@Service()
@RequiredArgsConstructor
public class OtpService {
    private final OtpRepository otpRepository;
    private final NexaService nexaService;
    private final EmailService emailService;
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final InstitutionConfigService institutionConfigService;
    private final ClientVerificationRepository clientVerificationRepository;
    private final MobilePaymentService mobilePaymentService;
    private final ClientRequestRepository clientRequestRepository;
    private final BetaSmsService betaSmsService;

    public void dateInit (){

    }

    public static int GenerateOtp() {
        Random random = new Random();
        return ((1 + random.nextInt(2)) * 10000 + random.nextInt(10000));
    }

    @Async()
    public void SendOtp(OtpEntity otpEntity, List<Object> payloads, InstitutionConfig config) {

        String smsMessage = "";

        String mailMessage = "";
        String mailSubject = "";
        payloads.add(otpEntity);

        if (otpEntity.getTransport().equals(NotificationChanel.BOTH)) {
            Optional<NotificationTemplate> smsTemplate = notificationTemplateRepository.findByNotificationTypeAndEventCode(NotificationChanel.SMS, String.valueOf(otpEntity.getRole()));
            Optional<NotificationTemplate> emailTemplate = notificationTemplateRepository.findByNotificationTypeAndEventCode(NotificationChanel.MAIL, String.valueOf(otpEntity.getRole()));

            if (smsTemplate.isPresent() && smsTemplate.get().getStatus().equals("ACTIVE")) {
                smsMessage = replaceParameters(smsTemplate.get().getTemplate(), payloads);

                otpEntity.setSubject(smsTemplate.get().getSubject());
//                var next = NexaResponse.builder().sms(smsMessage).mobiles(otpEntity.getPhoneNumber()).senderid(smsTemplate.get().getSubject()).build();
                var betaSms = BetaResponse.builder().message(smsMessage).destinations(otpEntity.getPhoneNumber()).externalReference(otpEntity.getUuid()).type("sms").build();
                betaSmsService.sendSms(betaSms);
//                nexaService.LessSms(next);
            }

            if (emailTemplate.isPresent() && emailTemplate.get().getStatus().equals("ACTIVE")) {
                mailMessage = replaceParameters(emailTemplate.get().getTemplate(), payloads);
                mailSubject = replaceParameters(emailTemplate.get().getSubject(), payloads);


                otpEntity.setSubject(mailSubject);
                otpRepository.save(otpEntity);

                emailService.sendSimpleMessage(otpEntity.getEmail(), mailSubject, mailMessage);
            }

        } else {
            Optional<NotificationTemplate> notificationTemplate = notificationTemplateRepository.findByNotificationTypeAndEventCode(otpEntity.getTransport(), String.valueOf(otpEntity.getRole()));

            if (notificationTemplate.isPresent() && notificationTemplate.get().getStatus().equals("ACTIVE")) {
                payloads.add(otpEntity);
                smsMessage = replaceParameters(notificationTemplate.get().getTemplate(), payloads);
                mailSubject = replaceParameters(notificationTemplate.get().getSubject(), payloads);

            } else {
                mailSubject = config.getInstitutionShortName();
                smsMessage = OtpMessageTemplate(otpEntity);
            }

            otpEntity.setSubject(mailSubject);
            otpRepository.save(otpEntity);

            if (otpEntity.getTransport().equals(NotificationChanel.SMS)) {
                var betaSms = BetaResponse.builder().message(smsMessage).destinations(otpEntity.getPhoneNumber()).externalReference(otpEntity.getUuid()).type("sms").build();
                betaSmsService.sendSms(betaSms);
//                var next = NexaResponse.builder().sms(smsMessage).mobiles(otpEntity.getPhoneNumber()).senderid(mailSubject).build();
//                nexaService.LessSms(next);
            } else if (otpEntity.getTransport().equals(NotificationChanel.MAIL)) {
                emailService.sendSimpleMessage(otpEntity.getEmail(), mailSubject, smsMessage);
            }
        }
        CompletableFuture.completedFuture(null);
    }
//    @Async()
//    public void Resend(OtpEntity otp) {
//        if (otp.getTransport().equals(NotificationChanel.SMS)) {
//            var next = NexaResponse.builder().sms("Your otp is" + otp.getOtp()).mobiles(otp.getPhoneNumber()).senderid(otp.getSubject()).build();
//            nexaService.LessSms(next);
//        } else if (otp.getTransport().equals(NotificationChanel.MAIL)) {
//            emailService.sendSimpleMessage(otp.getEmail(), otp.getSubject(), otp.getMessage());
//        } else if (otp.getTransport().equals(NotificationChanel.BOTH)) {
//            var next = NexaResponse.builder().sms("Your otp is" + otp.getOtp()).mobiles(otp.getPhoneNumber()).senderid(otp.getSubject()).build();
//            nexaService.LessSms(next);
//
//            emailService.sendSimpleMessage(otp.getEmail(), otp.getSubject(), "Your otp is" + otp.getOtp());
//        }
//        CompletableFuture.completedFuture(null);
//    }

    public CustomerVerification GenerateResendOtp(String uuid, Subscriptions subscriptions) throws Exception {
        Optional<OtpEntity> otpEntity = otpRepository.findByUuid(uuid);
        InstitutionConfig config = institutionConfigService.findByyApp(Application.MB.name());


        if (otpEntity.isEmpty()) {
            throw new ResourceNotFoundException("FAILED_TO_SEND_OTP");
        }
        OtpEntity otp = otpEntity.get();

        if (otp.getUsed()) {
            throw new ResourceNotFoundException("FAILED_TO_SEND_OTP");
        }

        ArrayList<Object> payloads = new ArrayList<>();
        payloads.add(subscriptions);

        if (otp.getRole().equals(OtpEnum.VALIDATE_TRANSACTION)) {
            MobilePayment initiatedPayment = mobilePaymentService.getPaymentBytUuidAndClient(otp.getGuid(), subscriptions.getClientMatricul())
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("invalid_transaction"));

            payloads.add(initiatedPayment);
        } else if (otp.getRole().equals(OtpEnum.OPEN_ACCOUNT_REQUEST)) {
            Optional<ClientRequest> request = clientRequestRepository.findByUuid(otp.getGuid());
            if (request.isEmpty()) {
                throw new ResourceNotFoundException("invalid_transaction");
            }
            payloads.add(request.get());
        }
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zdt = LocalDateTime.now().atZone(zoneId);
        zdt.toLocalDateTime().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
//        expiresAt = expiresAt.atZone(zoneId).toLocalDateTime();
        Long otpCode = (long) GenerateOtp();
        OtpEntity newOtp = OtpEntity.builder()
                .otp(otpCode)
                .role(otp.getRole())
                .expiresAt(LocalDateTime.now().plusMinutes(config.getOtpMinBeforeExpire()).atZone(zoneId).toOffsetDateTime())
                .minBeforeExpire(config.getOtpMinBeforeExpire())
                .transport(otp.getTransport())
                .email(otp.getEmail())
                .phoneNumber(otp.getPhoneNumber())
                .createdAt(zdt.toOffsetDateTime())
                .sent(false)
                .used(false)
                .guid(otp.getGuid())
                .build();
        CustomerVerification verification = institutionConfigService.countRemainingCustomerTrials(subscriptions, config,  VerificationType.OTP);

        otpRepository.deleteByRoleAndGuid(otp.getRole(), otp.getGuid());
        OtpEntity newOtpEntity = otpRepository.save(newOtp);
        verification.setVerificationObject(newOtpEntity);
        verification.setVerificationType("OTP");
        CompletableFuture.runAsync(() -> SendOtp(newOtpEntity, payloads, config));
        return verification;
    }

    public CustomerVerification GenerateAndSend(OtpEntity params, List<Object> payloads, Subscriptions subscriptions) throws ResourceNotFoundException, UnauthorizedUserException {
        InstitutionConfig config = institutionConfigService.findByyApp(Application.MB.name());
        Long otpCode = (long) GenerateOtp();
        if (params.getTransport().equals(NotificationChanel.MAIL) || params.getTransport().equals(NotificationChanel.BOTH)) {
            if (!PasswordConstraintValidator.isValidEmail(params.getEmail())) {
                throw new ResourceNotFoundException("Invalid Email Address");
            }
        }
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zdt = LocalDateTime.now().atZone(zoneId);
        zdt.toLocalDateTime().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        OtpEntity otp = OtpEntity.builder()
                .otp(otpCode)
                .role(params.getRole())
                .expiresAt(LocalDateTime.now().plusMinutes(config.getOtpMinBeforeExpire()).atZone(zoneId).toOffsetDateTime())
                .minBeforeExpire(config.getOtpMinBeforeExpire())
                .transport(params.getTransport())
                .email(params.getEmail())
                .phoneNumber(params.getPhoneNumber())
                .createdAt(zdt.toOffsetDateTime())
                .sent(false)
                .used(false)
                .guid(params.getGuid())
                .build();
        CustomerVerification verification = institutionConfigService.countRemainingCustomerTrials(subscriptions, config,  VerificationType.OTP);

        otpRepository.deleteByRoleAndGuid(otp.getRole(), otp.getGuid());

        OtpEntity otpEntity = otpRepository.save(otp);
        verification.setVerificationObject(otpEntity);
        verification.setVerificationType("OTP");
        CompletableFuture.runAsync(() -> SendOtp(otpEntity, payloads, config));
        return verification;

    }

    static String OtpMessageTemplate(OtpEntity otp) {
        String otpMessage = "";
        switch (otp.getRole()) {
            case DOUBLE_AUTHENTICATION -> otpMessage = "Your Authentication OTP is : " + otp.getOtp();
            case RESET_PASSWORD -> otpMessage = "Your ResetPassword OTP is :" + otp.getOtp();
        }
        return otpMessage;
    }

    public void verifyOtpOnGenerate(OtpEnum role, String guid) {
        var res = otpRepository.findByRoleAndGuid(role, guid);
        if (res.isPresent()) {
            System.out.println(res);
            if (res.get().getGuid().equals(guid) && res.get().getRole().equals(role)) {
                otpRepository.deleteById(res.get().getId());
            }
        }
    }

    public ClientVerification VerifyOtp(OtpAuth otp, String guid, Subscriptions sub,String ip) throws  UnauthorizedUserException {
        try {
            ZoneId zoneId = ZoneId.systemDefault();
            ZonedDateTime zdt = LocalDateTime.now().atZone(zoneId);
            zdt.toLocalDateTime().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

            Optional<OtpEntity> OtpSms = otpRepository.findByRoleAndGuidAndUsed(OtpEnum.valueOf(otp.getRole()), guid, false);
            if (OtpSms.isEmpty()) {
                throw new ResourceNotFoundException("Not found");
            }
            if (OtpSms.get().getExpiresAt().isBefore(LocalDateTime.now().atZone(zoneId).toOffsetDateTime())) {
                throw new ExpiredPasswordException("Your_Otp_expired");
            }

            if (OtpSms.get().getOtp() != otp.getOtp()) {
                throw new ResourceNotFoundException("Otp Does Not Match");
            }
            OtpSms.get().setUsed(true);
            OtpSms.get().setSent(true);

            otpRepository.save(OtpSms.get());
            institutionConfigService.archiveClientVerifications(sub, VerificationType.OTP);
            ClientVerification verify = ClientVerification.builder()
                    .subscriptions(sub)
                    .status(Status.SUCCESS)
                    .role(otp.getRole())
                    .verified(true)
                    .verificationType(VerificationType.OTP)
                    .phoneNumber(sub.getPhoneNumber())
                    .ip(ip)
                    .message("verified")
                    .build();
            clientVerificationRepository.save(verify);
            return clientVerificationRepository.save(verify);
        } catch (Exception ex) {
            ClientVerification verify = ClientVerification.builder()
                    .subscriptions(sub)
                    .status(Status.FAILED)
                    .role(otp.getRole())
                    .verified(false)
                    .verificationType(VerificationType.OTP)
                    .phoneNumber(sub.getPhoneNumber())
                    .ip(ip)
                    .message(ex.getMessage())
                    .build();
            clientVerificationRepository.save(verify);
            InstitutionConfig config = institutionConfigService.findByyApp(Application.MB.name());
            CustomerVerification verificationObject = institutionConfigService.countRemainingCustomerTrials(sub, config,  VerificationType.OTP);
            throw new FailedSecurityVerification(ex.getMessage(), verificationObject);
        }
    }

    public ClientVerification VerifyOtp(OtpAuth otp, OtpEntity OtpSms, Subscriptions sub,String ip) throws UnauthorizedUserException {
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zdt = LocalDateTime.now().atZone(zoneId);
        zdt.toLocalDateTime().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        if (OtpSms.getExpiresAt().isBefore(LocalDateTime.now().atZone(zoneId).toOffsetDateTime())) {
            throw new UnauthorizedUserException("your_otp_expired");
        }
        try {

            if (OtpSms.getOtp() != otp.getOtp()) {
                throw new ResourceNotFoundException("Otp Does Not Match");
            }
            OtpSms.setUsed(true);
            OtpSms.setSent(true);

            otpRepository.save(OtpSms);
            institutionConfigService.archiveClientVerifications(sub, VerificationType.OTP);

            ClientVerification verify = ClientVerification.builder()
                    .subscriptions(sub)
                    .status(Status.SUCCESS)
                    .role(otp.getRole())
                    .verified(true)
                    .phoneNumber(sub.getPhoneNumber())
                    .ip(ip)
                    .verificationType(VerificationType.OTP)
                    .message("verified")
                    .build();
            clientVerificationRepository.save(verify);
            return clientVerificationRepository.save(verify);
        } catch (Exception ex) {
            ClientVerification verify = ClientVerification.builder()
                    .subscriptions(sub)
                    .status(Status.FAILED)
                    .role(otp.getRole())
                    .verified(false)
                    .phoneNumber(sub.getPhoneNumber())
                    .ip(ip)
                    .verificationType(VerificationType.OTP)
                    .message(ex.getMessage())
                    .build();
            clientVerificationRepository.save(verify);
            InstitutionConfig config = institutionConfigService.findByyApp(Application.MB.name());
            CustomerVerification verificationObject = institutionConfigService.countRemainingCustomerTrials(sub, config,  VerificationType.OTP);

            throw new FailedSecurityVerification(ex.getMessage(), verificationObject);
        }
    }


    public Boolean VerifyOtp(OtpAuth otp, String guid) throws ResourceNotFoundException, ExpiredPasswordException {
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zdt = LocalDateTime.now().atZone(zoneId);
        zdt.toLocalDateTime().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        Optional<OtpEntity> Otp = otpRepository.findByRoleAndGuidAndUsed(OtpEnum.valueOf(otp.getRole()), guid, false);
        if (Otp.isEmpty()) {
            throw new ResourceNotFoundException("no_request_found");
        }
        if (Otp.get().getExpiresAt().isBefore(LocalDateTime.now().atZone(zoneId).toOffsetDateTime()))
            throw new ExpiredPasswordException("expired_otp");

        if (Otp.get().getOtp() != otp.getOtp()) {
            throw new ResourceNotFoundException("otp_don't_match");
        }
        Otp.get().setUsed(true);
        Otp.get().setSent(true);
        otpRepository.deleteByRoleAndGuid(Otp.get().getRole(), Otp.get().getGuid());
        otpRepository.save(Otp.get());
        return true;
    }

    public static String replaceParameters(String template, List<Object> payloads) {
        if (payloads.isEmpty()) {
            return template;
        }
        for (Object payload : payloads) {
            // Get the class of the User object
            Class<?> clazz = payload.getClass();

            // Iterate over declared fields in the class
            for (Field field : clazz.getDeclaredFields()) {
                // Get the field name
                String fieldName = field.getName();
                // Get the field value using reflection
                try {
                    field.setAccessible(true); // Allow accessing private fields
                    Object value = field.get(payload); // Get the value of the field
                    if (value != null) {
                        template = template.replace("%" + fieldName + "%", value.toString());
                    }
                } catch (IllegalAccessException ignored) {
                    System.out.println("111111111111111111111111 theres an error with field name   " + fieldName);
                }
            }
        }

        return template;
    }
}
