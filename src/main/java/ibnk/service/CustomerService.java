package ibnk.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ibnk.dto.*;
import ibnk.dto.BankingDto.AccountEntityDto;
import ibnk.dto.auth.*;
import ibnk.models.banking.Client;
import ibnk.models.banking.MobileBankConfiguration;
import ibnk.models.internet.ClientVerification;
import ibnk.models.internet.InstitutionConfig;
import ibnk.models.internet.OtpEntity;
import ibnk.models.internet.UserEntity;
import ibnk.models.internet.client.ClientDevice;
import ibnk.models.internet.client.ClientDeviceId;
import ibnk.models.internet.client.ClientRequest;
import ibnk.models.internet.client.Subscriptions;
import ibnk.models.internet.enums.*;
import ibnk.models.internet.security.AuditClientPassword;
import ibnk.repositories.banking.ClientMatriculRepository;
import ibnk.repositories.banking.MbConfigRepository;
import ibnk.repositories.internet.*;
import ibnk.service.BankingService.AccountService;
import ibnk.tools.TOOLS;
import ibnk.tools.error.*;
import ibnk.tools.jwtConfig.JwtService;
import ibnk.tools.response.AuthResponse;
import ibnk.tools.security.PasswordConstraintValidator;
import jakarta.annotation.PostConstruct;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

import static org.bouncycastle.crypto.tls.ConnectionEnd.client;

@RequiredArgsConstructor
@Service
public class CustomerService {
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;
    private final JwtService jwtService;
    private final Logger LOGGER = LogManager.getLogger(AuthenticationService.class);
    private final SubscriptionRepository subscriptionRepository;
    private final OtpRepository otpRepository;
    private final ClientVerificationRepository clientVerificationRepository;
    private final AccountService accountService;
    private final ClientSecurityQuestionRepository clientSecurityQuestionRepository;
    private final ClientDeviceRepository clientDeviceRepository;
    private final InstitutionConfigService institutionConfigService;

    private final ClientRequestRepository clientRequestRepository;

    private final MbConfigRepository mbConfigRepository;

    private final ClientMatriculRepository clientMatriculRepository;


    private final AuditClientPasswordRepository auditClientPasswordRepository;

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$^&*[]+=?";

    private static final String ALL_CHARS = UPPERCASE + LOWERCASE + DIGITS + SPECIAL_CHARS;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    @Qualifier("bankingJdbcTemplate")
    JdbcTemplate bankingJdbcTemplate;

    private SimpleJdbcCall validateSubscriptionCall;
    private SimpleJdbcCall validatePass;

    @PostConstruct
    public void init() {
        this.validateSubscriptionCall = new SimpleJdbcCall(bankingJdbcTemplate)
                .withProcedureName("PS_VALIDATE_INTBANKING");
        this.validatePass = new SimpleJdbcCall(bankingJdbcTemplate)
                .withProcedureName("PS_VALIDATE_PASS");

    }

    public static String generatePassword(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("Password length should be at least 8 characters");
        }

        StringBuilder password = new StringBuilder(length);

        // Ensure the password contains at least one of each type of character
        password.append(UPPERCASE.charAt(RANDOM.nextInt(UPPERCASE.length())));
        password.append(LOWERCASE.charAt(RANDOM.nextInt(LOWERCASE.length())));
        password.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        password.append(SPECIAL_CHARS.charAt(RANDOM.nextInt(SPECIAL_CHARS.length())));

        // Fill the remaining characters randomly
        for (int i = 4; i < length; i++) {
            password.append(ALL_CHARS.charAt(RANDOM.nextInt(ALL_CHARS.length())));
        }

        // Shuffle the characters to ensure randomness
        StringBuilder shuffledPassword = new StringBuilder(password.length());
        while (password.length() > 0) {
            int index = RANDOM.nextInt(password.length());
            shuffledPassword.append(password.charAt(index));
            password.deleteCharAt(index);
        }

        return shuffledPassword.toString();
    }

    public Subscriptions findClientByUuid(String uuid) {
        return subscriptionRepository.findByUuid(uuid).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }


    protected void updateClientMobileTypeAndPrimaryAccount(SubscriptionDao dao, String clientId, String EbnkSub) throws ResourceNotFoundException {
        int updatedColumns = clientMatriculRepository.updateClientEBankPackage(dao.getPackageCode(), dao.getAccountId(), EbnkSub, clientId);
        if (updatedColumns != 1) throw new ResourceNotFoundException(updatedColumns + "_more_than_one_updated");
    }


    @Transactional
    public UserDto.CreateSubscriberClientDto AdminSubscribe(SubscriptionDao dao, UserEntity user) throws ResourceNotFoundException, ValidationException {
        UserDto.CreateSubscriberClientDto dto = new UserDto.CreateSubscriberClientDto();
        List<AccountEntityDto> accountEntityDto = accountService.findClientAccounts(dao.getAccountId());
        AccountEntityDto accountInfo = accountEntityDto
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(dao.getAccountId() + " Does Not Exist Please Make Sure you have an Account"));

        Client matricul = clientMatriculRepository.findById(accountInfo.getClientID()).orElseThrow(() -> new UsernameNotFoundException("failed_login ."));

        Optional<Subscriptions> subscribed = subscriptionRepository.findByClientMatricul(accountInfo.getClientID());
        if (subscribed.isPresent()) throw new ResourceNotFoundException("customer_already_subscribed");

        subscribed = subscriptionRepository.findByUserLogin(dao.getUserLogin());
        if (subscribed.isPresent()) throw new ResourceNotFoundException("user_login_already_exist");

        if (accountInfo.getMobile() == null || accountInfo.getMobile().isEmpty()) {
            throw new ResourceNotFoundException("please_add_customer_phoneNumber");
        }

        if (!PasswordConstraintValidator.isValidEmail(accountInfo.getEmail()))
            throw new ResourceNotFoundException("Invalid-Email");
        if (accountInfo.getMobileType() == null || accountInfo.getMobileType().isBlank()) {
            Optional<MobileBankConfiguration> MbPackage = mbConfigRepository.findById(dao.getPackageCode());
            if (MbPackage.isEmpty()) throw new ResourceNotFoundException("mobile_configuration_not_found");
            updateClientMobileTypeAndPrimaryAccount(dao, accountInfo.getClientID(), "ST1");
        } else {
            if (!Objects.equals(accountInfo.getEaccount(), dao.getAccountId())) {
                throw new ResourceNotFoundException("Mobile Bank Primary Account is different do you want to continue");
            }
        }
        Subscriptions subscription = ClientToSubscribe(dto, accountInfo);
        subscription.setSubscriberBy(user);
        subscription.setFirstLogin(false);
        subscription.setContactVerification(true);
        subscription.setBranchCode(accountInfo.getBranchName());
        subscription.setUserLogin(dao.getUserLogin());
        subscription.setClient(matricul);
        subscription = handleSubscriptionValidation(dao, user.getUserLogin(), subscription);
        return UserDto.CreateSubscriberClientDto.modelToDao(subscription);
    }

    @Transactional
    public AuthResponse<UserDto.CreateSubscriberClientDto, CustomerVerification> SelfSubscription(SubscriptionDao dao, InstitutionConfig config) throws ResourceNotFoundException, ValidationException, UnauthorizedUserException, OtpSubscriberException {
        validateClientLogin(dao.getUserLogin());
        UserDto.CreateSubscriberClientDto dto = new UserDto.CreateSubscriberClientDto();

        List<Object> payloads = new ArrayList<>();

        AccountEntityDto accountInfo = getAccountInfo(dao.getAccountId());
        Client matricul = clientMatriculRepository.findById(accountInfo.getClientID()).orElseThrow(() -> new UsernameNotFoundException("failed_login ."));

        Optional<Subscriptions> subscribed;
        subscribed = subscriptionRepository.findByClientMatricul(accountInfo.getClientID());
        if (subscribed.isPresent()) {
            Subscriptions subscriber = subscribed.get();
            if (subscriber.getContactVerification()) throw new ValidationException("customer_already_subscribed");
            subscriber.setClient(matricul);
            subscriber.setUserLogin(dao.getUserLogin());
            subscriptionRepository.save(subscriber);

            payloads.add(UserDto.CreateSubscriberClientDto.modelToDao(subscriber));
            CustomerVerification verificationObject = otpService.GenerateAndSend(createOtpParams(subscriber), payloads, subscriber);
            UserDto.CreateSubscriberClientDto clientDto = UserDto.CreateSubscriberClientDto.modelToDao(subscriber);
            return new AuthResponse<>(clientDto, verificationObject);
        }

        if (matricul.getEmail().isEmpty() || matricul.getEmail().isBlank()) {
            throw new ValidationException("No email registered for " + dao.getAccountId());
        }

        if (matricul.getEmail().trim().isEmpty()) {
            throw new ValidationException("No email registered for " + dao.getAccountId());
        }
        if (matricul.getPhoneNumber().isEmpty() || matricul.getPhoneNumber().isBlank()) {
            throw new ValidationException("No phone registered for " + dao.getAccountId());
        }

        if (matricul.getPhoneNumber().trim().isEmpty()) {
            throw new ValidationException("No phone registered for " + dao.getAccountId());
        }


        subscribed = subscriptionRepository.findByUserLogin(dao.getUserLogin());
        if (subscribed.isPresent())
            throw new ValidationException("the userName " + dao.getUserLogin() + " already exist");


        if (accountInfo.getEBankingSub().trim().equals("No")) {
            validateSubscription(accountInfo, dao);
            handleSubscriptionRequest(dao, config, dto, accountInfo);
        }
        Subscriptions subscription = ClientToSubscribe(dto, accountInfo);
        subscription.setFirstLogin(false);
        subscription.setContactVerification(false);
        subscription.setBranchCode(accountInfo.getOurBranchID());
        subscription.setPrimaryAccount(accountInfo.getAccountID());
        subscription.setUserLogin(dao.getUserLogin());
        subscription.setClient(matricul);


        subscription = subscriptionRepository.save(subscription);

        OtpEntity otpParams = createOtpParams(subscription);
        payloads.add(UserDto.CreateSubscriberClientDto.modelToDao(subscription));
        CustomerVerification verificationObject = otpService.GenerateAndSend(otpParams, payloads, subscription);
        UserDto.CreateSubscriberClientDto clientDto = UserDto.CreateSubscriberClientDto.modelToDao(subscription);

        return new AuthResponse<>(clientDto, verificationObject);
    }

    private AccountEntityDto getAccountInfo(String accountId) throws ResourceNotFoundException {
        return accountService.findClientAccounts(accountId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(accountId + " Does Not Exist Please Make Sure you have an Account"));
    }

    private void validateSubscription(AccountEntityDto accountInfo, SubscriptionDao dao) throws ValidationException {

        if (subscriptionRepository.findByUserLogin(dao.getUserLogin()).isPresent()) {
            throw new ValidationException("user_login_already_exist");
        }
        if (!PasswordConstraintValidator.isValidEmail(accountInfo.getEmail())) {
            throw new ValidationException("invalid_email_contact_your_provider");
        }

        if (accountInfo.getMobile() == null || accountInfo.getMobile().isEmpty()) {
            throw new ValidationException("please_add_customer_phoneNumber");
        }


    }

    private void handleSubscriptionRequest(SubscriptionDao dao, InstitutionConfig config, UserDto.CreateSubscriberClientDto dto, AccountEntityDto accountInfo) throws ResourceNotFoundException {
        dao.setPackageCode(config.getDefaultPackage());
        updateClientMobileTypeIfNeeded(dao, config, accountInfo);

    }

    private void updateClientMobileTypeIfNeeded(SubscriptionDao dao, InstitutionConfig config, AccountEntityDto accountInfo) throws ResourceNotFoundException {
        if (accountInfo.getMobileType() == null || accountInfo.getMobileType().isBlank()) {
            Optional<MobileBankConfiguration> mbPackage = mbConfigRepository.findById(config.getDefaultPackage());
            if (mbPackage.isEmpty()) {
                throw new ResourceNotFoundException("mobile_configuration_not_found");
            }
            updateClientMobileTypeAndPrimaryAccount(dao, accountInfo.getClientID(), "ST1");
        } else {
            if (!Objects.equals(accountInfo.getEaccount(), dao.getAccountId())) {
                throw new ResourceNotFoundException("Mobile Bank Primary Account is different do you want to continue");
            }
        }
    }

    private OtpEntity createOtpParams(Subscriptions subscription) {
        return OtpEntity.builder()
                .email(subscription.getEmail())
                .phoneNumber(subscription.getPhoneNumber())
                .role(OtpEnum.SUBSCRIPTION_REQUEST)
                .transport(NotificationChanel.MAIL)
                .used(false)
                .guid(subscription.getUuid())
                .build();
    }

    public Subscriptions handleSubscriptionValidation(SubscriptionDao dao, String employee, Subscriptions subscription) throws ResourceNotFoundException, ValidationException {

        AccountEntityDto accountInfo = getAccountInfo(subscription.getPrimaryAccount());
        String generatedPassword = generatePassword(8);

        if (accountInfo.getEBankingSub().trim().equals("No")) {
            String valid = validateInternetBankingSubscription(dao, employee, subscription.getClientMatricul());

            if (valid == null) {
                dao.setPackageCode(null);
                dao.setAccountId(null);
                updateClientMobileTypeAndPrimaryAccount(dao, subscription.getClientMatricul(), null);
                throw new ValidationException("unable_to_validate_subscription");
            }
        }
        List<Object> payload = new ArrayList<>();
        UserDto.CreateSubscriberClientDto user = UserDto.CreateSubscriberClientDto.modelToDao(subscription);

        user.setGeneratedPassword(generatedPassword);
        payload.add(user);
        NotificationEvent event = new NotificationEvent();
        event.setEventCode(EventCode.SUBSCRIPTION_PASSWORD.name());
        event.setPayload(payload);
        event.setType(NotificationChanel.MAIL);
        event.setEmail(subscription.getEmail());
        event.setPhoneNumber(subscription.getPhoneNumber());
        applicationEventPublisher.publishEvent(event);

        subscription.setPassword(passwordEncoder.encode(generatedPassword));
        subscription.setStatus(SubscriberStatus.ACTIVE.name());

        AuditClientPassword passwordHistory = new AuditClientPassword();
        passwordHistory.setPassword(passwordEncoder.encode(generatedPassword));
        passwordHistory.setSubscriber(subscription);
        this.auditClientPasswordRepository.save(passwordHistory);
        return subscriptionRepository.save(subscription);
    }

    public String validateInternetBankingSubscription(SubscriptionDao dao, String employee, String client) {
        MapSqlParameterSource in = new MapSqlParameterSource()
                .addValue("ws_client", client)
                .addValue("Type", 2)
                .addValue("takefee", dao.getApplyFee())
                .addValue("wEmploye", employee)
                .addValue("Language", "En")
                .addValue("Computername", "INTERNET BANKING")
                .addValue("pc_OutLECT", null)
                .addValue("pc_OutMSG", null)
                .addValue("Password", null);
        try {
            Map<String, Object> out = validateSubscriptionCall.execute(in);
            if ((Integer) out.get("pc_OutLECT") != 0) {
                throw new ValidationException((String) out.get("pc_OutMSG"));
            }
            return (String) out.get("Password");
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public Integer validatePass(String pass, Integer client) {
        MapSqlParameterSource in = new MapSqlParameterSource()
                .addValue("ws_client", client)
                .addValue("pass", pass);
        try {
            Map<String, Object> out = validatePass.execute(in);
            if ((Integer) out.get("isValid") != 0) {
                throw new ValidationException("Failed_Login");
            }
            return (int) out.get("isValid");
        } catch (Exception e) {
            return 0;
        }
    }

    public AuthResponse<Object, Object> authenticate(AuthDto authDto, HttpServletRequest request) throws UnauthorizedUserException, JsonProcessingException {
        LOGGER.info("Enter >> Client Authentication Function");
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authDto.getUserLogin(), authDto.getPassword()));
        Object client1 = authentication.getPrincipal();
        if (client1 instanceof UserEntity user) throw new UnauthorizedUserException("invalid account type");

        Subscriptions client = (Subscriptions) client1;
        InstitutionConfig config = institutionConfigService.findByyApp(Application.MB.name());


        String deviceHeader = request.getHeader("W");
        ObjectMapper objectMapper = new ObjectMapper();
        ClientDeviceDto headerDevice = objectMapper.readValue(deviceHeader, ClientDeviceDto.class);

        ClientDeviceId deviceId = new ClientDeviceId(client, headerDevice.getDeviceId(), headerDevice.getDeviceToken());
        Optional<ClientDevice> clientDevice = clientDeviceRepository.findById(deviceId);

        try {

            if (client.getStatus() == null) {
                throw new UnauthorizedUserException("invalid username or password");
            }

            institutionConfigService.countRemainingCustomerTrials(client, config, VerificationType.USER);
            UserDto.CreateSubscriberClientDto clientDto = UserDto.CreateSubscriberClientDto.modelToDao(client);

            if (client.getPasswordResetRequest() != null) {
                client.setPasswordResetRequest(null);
                subscriptionRepository.save(client);
            }

            clientDto.setSecurityQuestionCounts(clientSecurityQuestionRepository.countBySubscriptions(client));

            Object jwtToken;
            List<Object> payloads = new ArrayList<>();
            if (!client.getFirstLogin()) {

                if (client.getStatus().equals(SubscriberStatus.SUSPENDED.name())) {
                    throw new UnauthorizedUserException("account is suspended");
                }

                if (client.getStatus().equals(SubscriberStatus.BLOCKED.name())) {
                    throw new UnauthorizedUserException("account is blocked");
                }

                if (!client.getStatus().equals(SubscriberStatus.ACTIVE.name())) {
                    throw new UnauthorizedUserException("account is not activated");
                }

                int verifyMb = clientMatriculRepository.verifyPasswordAndClient(client.getClientMatricul(), authDto.getPassword());

                if (verifyMb != 1) {
                    ClientVerification verify = ClientVerification.builder()
                            .subscriptions(client)
                            .status(Status.FAILED)
                            .role("USER LOGIN")
                            .verified(false)
                            .verificationType(VerificationType.USER)
                            .ip(ip(request))
                            .phoneNumber(client.getPhoneNumber())
                            .message("invalid password on login")
                            .build();
                    clientVerificationRepository.save(verify);
                    institutionConfigService.countRemainingCustomerTrials(client, config, VerificationType.USER);
                    throw new UnauthorizedUserException("invalid username or password ");
                }

//                if (config.getMinSecurityQuest() > 0) {
//
//                    if (clientDto.getSecurityQuestionCounts() > 0)
//                        throw new UnauthorizedUserException("unauthorized");
//                }



                OtpEntity otpParams = OtpEntity.builder()
                        .email(client.getEmail())
                        .phoneNumber(client.getPhoneNumber())
                        .role(OtpEnum.FIRST_LOGIN)
                        .transport(client.getPreferedNotificationChanel())
                        .used(false)
                        .guid(client.getUuid())
                        .build();

                if(clientDevice.isEmpty()) {
                    ClientDevice device = ClientDeviceDto.mapToEntity(headerDevice);
                    device.setId(deviceId);
                    device.setIsTrusted(false);
                    clientDeviceRepository.save(device);
                }

                payloads.add(UserDto.CreateSubscriberClientDto.modelToDao(client));
                CustomerVerification verificationObject = otpService.GenerateAndSend(otpParams, payloads, client);

                institutionConfigService.archiveClientVerifications(client, VerificationType.USER);

                System.out.println("Exit2 >> Authentication Function");
                return new AuthResponse<>(clientDto, verificationObject);
            }
            else {

                if(config.isVerifyDevice()) {
                    if(clientDevice.isEmpty()) {
                        throw new UnauthorizedUserException("unauthorized device");
                    }
                    if(!clientDevice.get().getIsActive() || !clientDevice.get().getIsTrusted()) {
                        throw new UnauthorizedUserException("unauthorized device");
                    }
                }

                if (client.getStatus().equals(SubscriberStatus.SUSPENDED.name())) {
                    throw new UnauthorizedUserException("account_suspended");
                }

                if (client.getStatus().equals(SubscriberStatus.BLOCKED.name())) {
                    throw new UnauthorizedUserException("account_blocked");
                }

                if (!client.getStatus().equals(SubscriberStatus.ACTIVE.name())) {
                    throw new UnauthorizedUserException("account is not activated");
                }

                boolean match = passwordEncoder.matches(authDto.getPassword(), client.getPassword());

                if (!match) {
                    ClientVerification verify = ClientVerification.builder()
                            .subscriptions(client)
                            .status(Status.FAILED)
                            .role("USER LOGIN")
                            .verified(false)
                            .verificationType(VerificationType.USER)
                            .ip(ip(request))
                            .phoneNumber(client.getPhoneNumber())
                            .message("failed password verification")
                            .build();
                    clientVerificationRepository.save(verify);

                    institutionConfigService.countRemainingCustomerTrials(client, config, VerificationType.USER);

                    throw new UnauthorizedUserException("invalid username or password");
                }

                if (Objects.equals(client.getPins(), null) || Objects.equals(client.getPins(), "")) {
                    OtpEntity otpParams = OtpEntity.builder()
                            .email(client.getEmail())
                            .phoneNumber(client.getPhoneNumber())
                            .role(OtpEnum.RESET_PIN)
                            .transport(client.getPreferedNotificationChanel())
                            .used(false)
                            .guid(client.getUuid())
                            .build();

                    payloads.add(UserDto.CreateSubscriberClientDto.modelToDao(client));
                    CustomerVerification verificationObject = otpService.GenerateAndSend(otpParams, payloads, client);

                    institutionConfigService.archiveClientVerifications(client, VerificationType.USER);

                    client.setPasswordResetRequest("ALLOW");
                    subscriptionRepository.save(client);

                    System.out.println("Exit2 >> Authentication Function");
                    return new AuthResponse<>(clientDto, verificationObject);
                }

                if (!client.getContactVerification()) {
                    throw new UnauthorizedUserException("contact verification wrong");
                }


                if(clientDevice.isPresent()) {
                    clientDevice.get().setLastLoginTime(LocalDateTime.now());
                    clientDeviceRepository.save(clientDevice.get());
                } else {
                    ClientDevice device = ClientDeviceDto.mapToEntity(headerDevice);
                    device.setId(deviceId);
                    device.setLastLoginTime(LocalDateTime.now());
                    device.setIpAddress(ip(request));
                    clientDeviceRepository.save(device);
                }

                jwtToken = jwtService.generateTokenForClient(client);
                institutionConfigService.archiveClientVerifications(client, VerificationType.USER);

                System.out.println("Exit3 >> Authentication Function");
                return new AuthResponse<>(clientDto, jwtToken);
            }
        } catch (FailedSecurityVerification e) {
            throw e;
        } catch (Exception e) {
            throw new FailedLoginException(e.getMessage(), client.getUuid(), e.getMessage(), "CLIENT");
        }
    }

    public ClientDevice clientDeviceCheck(HttpServletRequest request, Subscriptions subscription) throws  UnauthorizedUserException {
        String deviceHeader = request.getHeader("W");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ClientDeviceDto deviceInfo = objectMapper.readValue(deviceHeader, ClientDeviceDto.class);
            ClientDeviceId deviceId = new ClientDeviceId(subscription, deviceInfo.getDeviceId(), deviceInfo.getDeviceToken());

            Optional<ClientDevice> existingDevice = clientDeviceRepository.findById(deviceId);

            if(existingDevice.isEmpty()) {
                throw new UnauthorizedUserException("");
            }
            if(!existingDevice.get().getIsActive() || existingDevice.get().getIsTrusted()) {
                throw new UnauthorizedUserException("");
            }

            return  existingDevice.get();

        } catch (Exception e) {
            throw new UnauthorizedUserException(e.getMessage());
        }

    }

    public AuthResponse<Object, Object> OauthWithOtp(String guid, OtpAuth otpauth, HttpServletRequest request) throws UnauthorizedUserException {

        Optional<OtpEntity> otpEntity = otpRepository.findByUuidAndUsed(guid, false);
        if (otpEntity.isEmpty()) throw new UnauthorizedUserException("failed_login");

        Optional<Subscriptions> subscriber = subscriptionRepository.findByUuid(otpEntity.get().getGuid());
        if (subscriber.isEmpty()) throw new UnauthorizedUserException("failed_login");

        InstitutionConfig config = institutionConfigService.findByyApp(Application.MB.name());

        if(config.isVerifyDevice()) {
            ClientDevice clientDevice = clientDeviceCheck(request,subscriber.get());
            clientDevice.setLastLoginTime(LocalDateTime.now());
            clientDeviceRepository.save(clientDevice);
        }


        String ip = ip(request);
        ClientVerification verification = otpService.VerifyOtp(otpauth, otpEntity.get(), subscriber.get(), ip);

        UserDto.CreateSubscriberClientDto clientDto = UserDto.CreateSubscriberClientDto.modelToDao(subscriber.get());
        clientDto.setSecurityQuestionCounts(clientSecurityQuestionRepository.countBySubscriptions(subscriber.get()));

        Object jwtToken = jwtService.generateTokenForClient(subscriber.get());

        return new AuthResponse<>(clientDto, jwtToken);

    }

    @Transactional
    public String UpdatePassword(Subscriptions user, UpdatePasswordDto pass, HttpServletRequest requestIp) throws ValidationException, UnauthorizedUserException {
        InstitutionConfig config = institutionConfigService.findByyApp(Application.MB.name());
        Subscriptions subscriptions = findClientByUuid(user.getUuid());
        boolean isNewPass = pass.getNewPassword().matches(pass.getConfirmPassword());

        if (!isNewPass) {
            throw new ValidationException("NewPassword does not match ConfirmPassword.");
        }
        boolean isPasswordMatch = passwordEncoder.matches(pass.getOldPassword(),
                subscriptions.getPassword());
        if (!isPasswordMatch) {
            ClientVerification verify = ClientVerification.builder()
                    .subscriptions(user)
                    .status(Status.FAILED)
                    .role("USER LOGIN")
                    .verified(false)
                    .verificationType(VerificationType.UPDATE_PASSWORD)
                    .ip(ip(requestIp))
                    .phoneNumber(user.getPhoneNumber())
                    .message("Old Password does not match failed password verification")
                    .build();
            clientVerificationRepository.save(verify);

            CustomerVerification verificationObject = institutionConfigService.countRemainingCustomerTrials(user, config, VerificationType.UPDATE_PASSWORD);

            throw new FailedSecurityVerification("Old Password does not match", verificationObject);

        }

        boolean isOldPassword = this.isPasswordReused(user, pass.getConfirmPassword());
        if (isOldPassword) throw new ValidationException("new password cannot be same as old password");

        pass.setNewPassword(passwordEncoder.encode(pass.getConfirmPassword()));
        user.setPassword(pass.getNewPassword());
        subscriptionRepository.save(user);
        AuditClientPassword passwordHistory = new AuditClientPassword();
        passwordHistory.setPassword(passwordEncoder.encode(pass.getConfirmPassword()));
        passwordHistory.setSubscriber(user);
        this.auditClientPasswordRepository.save(passwordHistory);
        return "Updated";
    }

    public String UpdatePin(Subscriptions user, UpdatePasswordDto.UpdatePinDto pin, HttpServletRequest requestIp) throws ValidationException, UnauthorizedUserException {
        try {
            validatePin(pin.getNewPin(), pin.getConfirmPin());
        } catch (IllegalArgumentException e) {
            throw new ValidationException(e.getMessage());
        }
        InstitutionConfig config = institutionConfigService.findByyApp(Application.MB.name());
        Subscriptions subscriptions = findClientByUuid(user.getUuid());
        boolean isNewPin = pin.getNewPin().matches(pin.getConfirmPin());

        if (!isNewPin) {
            throw new ValidationException("NewPin does not match ConfirmPassword.");
        }
        boolean isPinMatch = passwordEncoder.matches(pin.getOldPin(),
                subscriptions.getPins());

        boolean isPinReUse = passwordEncoder.matches(pin.getConfirmPin(),
                subscriptions.getPins());

        if (!isPinMatch) {
            ClientVerification verify = ClientVerification.builder()
                    .subscriptions(user)
                    .status(Status.FAILED)
                    .role("USER LOGIN")
                    .verified(false)
                    .verificationType(VerificationType.UPDATE_PIN)
                    .ip(ip(requestIp))
                    .phoneNumber(user.getPhoneNumber())
                    .message("Old Pin does not match ")
                    .build();
            clientVerificationRepository.save(verify);

            CustomerVerification verificationObject = institutionConfigService.countRemainingCustomerTrials(user, config, VerificationType.UPDATE_PIN);

            throw new FailedSecurityVerification("Old Pin does not match", verificationObject);

        }

        if (isPinReUse) {
            throw new ValidationException("New pin has to be different from old Pin");
        }
        pin.setNewPin(passwordEncoder.encode(pin.getConfirmPin()));
        user.setPins(pin.getNewPin());
        subscriptionRepository.save(user);

        return "Updated";
    }


    public AuthResponse<Object, Object> forgotPassword(String login) throws UnauthorizedUserException, ResourceNotFoundException, ValidationException {
        Optional<Subscriptions> subscriber = subscriptionRepository.findByUserLogin(login);
        if (subscriber.isEmpty()) {
            throw new UnauthorizedUserException("User does not Exist");
        }
        Optional<Client> matricul = clientMatriculRepository.findById(subscriber.get().getClientMatricul());

        if (matricul.isEmpty()) {
            throw new UnauthorizedUserException("User does not Exist");
        }

        if (!subscriber.get().getStatus().equals("ACTIVE"))
            throw new ValidationException("the account is " + subscriber.get().getStatus());
        subscriber.get().setPasswordResetRequest("INITIATED");
        subscriber.get().setClient(matricul.get());
        subscriptionRepository.save(subscriber.get());
        var result = UserDto.CreateSubscriberClientDto.modelToDao(subscriber.get());
        OtpEntity otpParams = OtpEntity.builder()
                .email(result.getEmail())
                .phoneNumber(result.getPhoneNumber())
                .role(OtpEnum.RESET_PASSWORD)
                .used(false)
                .transport(subscriber.get().getPreferedNotificationChanel())
                .guid(result.getUuid())
                .build();
        List<Object> payloads = new ArrayList<>();
        payloads.add(result);
        CustomerVerification verificationObject = otpService.GenerateAndSend(otpParams, payloads, subscriber.get());

        return new AuthResponse<>(result, verificationObject);
    }


    public AuthResponse<Object, Object> setPin(String guid, ForgotPasswordDto.PinDto dto,HttpServletRequest  request) throws UnauthorizedUserException, ValidationException, JsonProcessingException {


        try {
            validatePin(dto.getPin(), dto.getConfirmPin());
        } catch (IllegalArgumentException e) {
            throw new ValidationException(e.getMessage());
        }

        Optional<ClientVerification> clientVerification = clientVerificationRepository.findByUuid(guid);
        if (clientVerification.isEmpty()) {
            throw new ValidationException("Unauthorized");
        }

        if (!clientVerification.get().isVerified()) {
            throw new ValidationException("Unauthorized");
        }

        if (!clientVerification.get().getVerificationType().name().equals(VerificationType.OTP.name())) {
            throw new ValidationException("Unauthorized");
        }

        if (clientVerification.get().getCreatedAt().plusMinutes(5L).isBefore(LocalDateTime.now())) {
            throw new ValidationException("your session expired!");
        }


        Subscriptions subscriber = clientVerification.get().getSubscriptions();
        String deviceHeader = request.getHeader("W");
        ObjectMapper objectMapper = new ObjectMapper();
        ClientDeviceDto headerDevice = objectMapper.readValue(deviceHeader, ClientDeviceDto.class);

        InstitutionConfig config = institutionConfigService.findByyApp(Application.MB.name());
        ClientDeviceId deviceId = new ClientDeviceId(subscriber, headerDevice.getDeviceId(), headerDevice.getDeviceToken());

        Optional<ClientDevice> clientDevice = clientDeviceRepository.findById(deviceId);

        if(clientDevice.isEmpty()) {
            throw  new UnauthorizedUserException("");
        }

        if (config.isVerifyDevice()) {
            if(!clientDevice.get().getIsActive() || !clientDevice.get().getIsTrusted()) {
                throw new UnauthorizedUserException("");
            }
        }

        if (subscriber.getPassword() == null || subscriber.getPassword().isEmpty() || subscriber.getPassword().isBlank())
            throw new UnauthorizedUserException("");
        Optional<Client> clientMatricul = clientMatriculRepository.findById(subscriber.getClientMatricul());

        if (clientMatricul.isEmpty()) {
            throw new UnauthorizedUserException("Unauthorized");
        }

        if (!subscriber.getStatus().equals("ACTIVE")) throw new ValidationException("the account is " + subscriber.getStatus());

        subscriber.setClient(clientMatricul.get());
        subscriber.setPins(passwordEncoder.encode(dto.getPin()));
        subscriptionRepository.save(subscriber);
        institutionConfigService.archiveClientVerifications(subscriber, VerificationType.OTP);
        Object jwtToken = jwtService.generateTokenForClient(subscriber);

        var result = UserDto.CreateSubscriberClientDto.modelToDao(subscriber);


        clientDevice.get().setLastLoginTime(LocalDateTime.now());
        clientDevice.get().setIsActive(true);
        clientDevice.get().setIsTrusted(true);
        ClientDeviceId newDeviceId = new ClientDeviceId(subscriber, headerDevice.getDeviceId(), headerDevice.getDeviceToken());
        clientDevice.get().setId(newDeviceId);
        clientDeviceRepository.save(clientDevice.get());

        return new AuthResponse<>(result, jwtToken);
    }

    @Transactional
    public String setPassword(String guid, ForgotPasswordDto pass, HttpServletRequest request) throws ValidationException, JsonProcessingException {


        boolean passwordMatch = pass.getNewPassword().matches(pass.getConfirmPassword());
        if (!passwordMatch) {
            throw new ValidationException("Password does not match.");
        }
        PasswordConstraintValidator.isAcceptablePassword(pass.getNewPassword());
        Optional<ClientVerification> clientVerification = clientVerificationRepository.findByUuid(guid);

        if (clientVerification.isEmpty()) {
            throw new ValidationException("Unauthorized");
        }
        if (!clientVerification.get().isVerified()) {
            throw new ValidationException("Unauthorized");
        }
        if (!clientVerification.get().getVerificationType().name().equals(VerificationType.OTP.name())) {
            throw new ValidationException("Unauthorized");
        }
        if (clientVerification.get().getCreatedAt().plusMinutes(5L).isBefore(LocalDateTime.now())) {
            throw new ValidationException("Unauthorized");
        }

        Subscriptions subscriber = clientVerification.get().getSubscriptions();

        String deviceHeader = request.getHeader("W");
        ObjectMapper objectMapper = new ObjectMapper();
        ClientDeviceDto headerDevice = objectMapper.readValue(deviceHeader, ClientDeviceDto.class);

        ClientDeviceId deviceId = new ClientDeviceId(subscriber, headerDevice.getDeviceId(), headerDevice.getDeviceToken());
        Optional<ClientDevice> clientDevice = clientDeviceRepository.findById(deviceId);

        if(clientDevice.isEmpty()) throw new ValidationException("Unauthorized");



        if (!Objects.equals(subscriber.getPasswordResetRequest(), "ALLOW")) {
            throw new ValidationException("Unauthorized" + subscriber.getPasswordResetRequest());
        }

        boolean isOldPassword = this.isPasswordReused(subscriber, pass.getConfirmPassword());
        if (isOldPassword) throw new ValidationException("new password cannot be same as old password");

        subscriber.setPassword(passwordEncoder.encode(pass.getConfirmPassword()));
        subscriber.setPasswordResetRequest(null);
        subscriber.setPasswordChangedTime(LocalDateTime.now());
        subscriber.setFirstLogin(true);
        subscriptionRepository.save(subscriber);

        AuditClientPassword passwordHistory = new AuditClientPassword();
        passwordHistory.setPassword(passwordEncoder.encode(pass.getConfirmPassword()));
        passwordHistory.setSubscriber(subscriber);
        this.auditClientPasswordRepository.save(passwordHistory);

        clientDevice.get().setIsTrusted(true);
        clientDeviceRepository.save(clientDevice.get());

        return "success";
    }

    /**
     * Validates the provided PIN and confirmation PIN.
     *
     * @param pin        The PIN to validate.
     * @param confirmPin The confirmation PIN to validate.
     * @throws IllegalArgumentException if validation fails.
     */
    private void validatePin(String pin, String confirmPin) {
        // Check if the PIN is exactly 5 digits
        if (pin == null || !pin.matches("\\d{5}")) {
            throw new IllegalArgumentException("PIN must be exactly 5 digits long.");
        }

        // Check for repetitive patterns (e.g., "11111")
        if (pin.chars().distinct().count() == 1) {
            throw new IllegalArgumentException("PIN cannot contain repetitive patterns like '11111'.");
        }

        // Check for sequential patterns (e.g., "12345" or "54321")
        if (isSequential(pin)) {
            throw new IllegalArgumentException("PIN cannot contain sequential patterns like '12345' or '54321'.");
        }

        // Check if confirmPin matches the pin
        if (!pin.equals(confirmPin)) {
            throw new IllegalArgumentException("Confirm PIN does not match the PIN.");
        }
    }

    /**
     * Checks if a PIN is sequential (e.g., "12345" or "54321").
     *
     * @param pin The PIN to check.
     * @return true if the PIN is sequential, false otherwise.
     */
    private boolean isSequential(String pin) {
        boolean isAscending = true;
        boolean isDescending = true;

        for (int i = 1; i < pin.length(); i++) {
            int diff = pin.charAt(i) - pin.charAt(i - 1);
            if (diff != 1) {
                isAscending = false;
            }
            if (diff != -1) {
                isDescending = false;
            }
        }

        return isAscending || isDescending;
    }

    public UserDto.CreateSubscriberClientDto findSubscriberByClientId(String clientId) {
        Optional<Subscriptions> subscription = subscriptionRepository.findByClientMatricul(clientId);
        Optional<Client> matricul = clientMatriculRepository.findById(clientId);
        subscription.ifPresent(subscriptions -> subscriptions.setClient(matricul.get()));
        return UserDto.CreateSubscriberClientDto.modelToDao(subscription.get());
    }

    @Transactional()
    public String updateClientProfile(UserDto.CreateSubscriberClientDto dto, Subscriptions subs) throws ResourceNotFoundException, UnauthorizedUserException {
        Subscriptions subscriptions = findClientByUuid(subs.getUuid());
        dto.setId(subs.getId());
        ValidateConstraints(dto);
        subscriptions.setUserLogin(dto.getUserName());
        subscriptions.setDoubleAuthentication(dto.getDoubleAuthentication());
        subscriptions.setPassExpiration(dto.getPassExpiration());
        subscriptions.setPassDuration(dto.getPassDuration());
        subscriptions.setAddress(dto.getAddress());
        subscriptions.setPassPeriodicity(dto.getPassPeriodicity());
        subscriptions.setPreferedNotificationChanel(NotificationChanel.valueOf(dto.getPreferredOtpChannel()));
        subscriptionRepository.save(subscriptions);

        InstitutionConfig config = institutionConfigService.getInstConfig();

        if (config.isCustomerUpdateContact()) {

        }
        return "Updated";
    }

    public String updatePrimaryAccount(UserDto.CreateSubscriberClientDto dto, Subscriptions subs) throws ResourceNotFoundException {
        Subscriptions subscriptions = findClientByUuid(subs.getUuid());
        dto.setId(subs.getId());
        subscriptions.setPrimaryAccount(dto.getPrimaryAccount());
        subscriptionRepository.save(subscriptions);
        return "Updated";
    }

    @Transactional
    public String updateClientStatus(clientSecurityUpdateDto body, String clientId) {
        Subscriptions subscriptions = subscriptionRepository.findByClientMatricul(clientId).orElseThrow();
        subscriptions.setStatus(body.getStatus());
//        if(PasswordConstraintValidator.isValidEmail(body.getEmail())){
//            subscriptions.setEmail(body.getEmail());
//        }
        subscriptionRepository.save(subscriptions);
        if (body.getStatus().equals("ACTIVE")) {
            clientVerificationRepository.deleteBySubscriptions(subscriptions);
        }
        return "Updated";
    }

    @Transactional
    public String resetClientSecurityQuestions(String clientId) {
        Subscriptions subscriptions = subscriptionRepository.findByClientMatricul(clientId).orElseThrow();
        subscriptions.setFirstLogin(false);
        clientVerificationRepository.deleteBySubscriptions(subscriptions);
        clientSecurityQuestionRepository.deleteClientSecurityQuestionsBySubscriptions(subscriptions);
        return "updated successfully";
    }

    public void ValidateConstraints(UserDto.CreateSubscriberClientDto dto) throws ResourceNotFoundException, UnauthorizedUserException {
        PasswordConstraintValidator.isAcceptableTelephone(dto.getPhoneNumber());
        if (subscriptionRepository.findByUserLogin(dto.getUserName()).isPresent() && dto.getId() == null) {
            throw new ResourceNotFoundException("UserName Already Exist");
        }
        if (dto.getEmail().isEmpty())
            throw new ResourceNotFoundException("Make Sure You have an Email With the Institution Before Proceeding with Subscription");

    }

    public DataTable AllSubscribers(int pageNumber, int pageSize, String sortDirection, String sortProperty, String propertyValue, String type, String status) {
        Subscriptions exampleRequest = new Subscriptions();

        // Use Java Reflection to set the property dynamically
        try {
            BeanUtils.setProperty(exampleRequest, sortProperty, propertyValue);
            BeanUtils.setProperty(exampleRequest, "customerType", type);
            BeanUtils.setProperty(exampleRequest, "status", status);
        } catch (Exception e) {

            // Handle reflection exception
            return null; // or handle the error accordingly
        }

        // Create ExampleMatcher to match by ignoring case and matching substrings
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.STARTING);

        // Create Example with the dynamically created example user and the matcher
        Example<Subscriptions> example = Example.of(exampleRequest, matcher);

        Pageable pageList = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.valueOf(sortDirection.toUpperCase()), sortProperty));
        Page<Subscriptions> pages = subscriptionRepository.findAll(example, pageList);
        DataTable table = new DataTable();
        List<UserDto.CreateSubscriberClientDto> requests = pages.getContent().stream().map(UserDto.CreateSubscriberClientDto::modelToDao).toList();
        table.setTotalPages(pages.getTotalPages());
        table.setTotalElements(pages.getTotalElements());
        table.setData(requests);
        return table;
    }

    private Subscriptions ClientToSubscribe(UserDto.CreateSubscriberClientDto subDto, AccountEntityDto accountEntityDto) {
        subDto.setName(accountEntityDto.getClientName());
        subDto.setEmail(accountEntityDto.getEmail());
        subDto.setSubscriptionDate(LocalDateTime.now());
        subDto.setPrimaryAccount(accountEntityDto.getAccountID());
        subDto.setClientMatricule(accountEntityDto.getClientID());
        subDto.setProductName(accountEntityDto.getAccountName());
        subDto.setPreferredOtpChannel(NotificationChanel.MAIL.name());
        subDto.setPhoneNumber(accountEntityDto.getMobile());
        subDto.setPassExpiration(false);
        subDto.setPassDuration(null);
        subDto.setPassPeriodicity(null);
        subDto.setDoubleAuthentication(false);
        return UserDto.CreateSubscriberClientDto.DtoToModel(subDto);
    }

    public boolean VerifyAccountRequest(OtpAuth otp, String guid) throws ResourceNotFoundException, UnauthorizedUserException, ExpiredPasswordException {
        Optional<ClientRequest> accountRequest = clientRequestRepository.findByUuid(guid);
        if (accountRequest.isEmpty()) throw new UnauthorizedUserException("no_request_found");
        boolean verified = otpService.VerifyOtp(otp, guid);
        if (verified) {
            accountRequest.get().setContactVerification(true);
            clientRequestRepository.save(accountRequest.get());
        }
        return verified;
    }

    public AuthResponse<Object, Object>


    verifyFirstLogin(OtpAuth otp, String guid, HttpServletRequest request) throws  UnauthorizedUserException {


        Optional<OtpEntity> otpEntity = otpRepository.findByUuidAndUsed(guid, false);
        if (otpEntity.isEmpty()) throw new UnauthorizedUserException("failed_login");

        Optional<Subscriptions> client = subscriptionRepository.findByUuid(otpEntity.get().getGuid());
        if (client.isEmpty()) throw new UnauthorizedUserException("failed_login");
        if (client.get().getFirstLogin() && client.get().getPins() != null)
            throw new UnauthorizedUserException("failed_login");

        String ip = ip(request);
        ClientVerification verification = otpService.VerifyOtp(otp, otpEntity.get(), client.get(), ip);

        client.get().setContactVerification(true);

        System.out.println("Exit3 >> Authentication Function");

        UserDto.CreateSubscriberClientDto clientDto = UserDto.CreateSubscriberClientDto.modelToDao(client.get());
        //    TODO    clientDto.setSecurityQuestionCounts(clientSecurityQuestionRepository.countBySubscriptions(client.get()));


        client.get().setPasswordResetRequest("ALLOW");
        subscriptionRepository.save(client.get());

        return new AuthResponse<>(clientDto, verification);
    }

    public String ip(HttpServletRequest request) {
        InstitutionConfig config = institutionConfigService.findByyApp(Application.MB.name());

        List<String> proxyList = new ArrayList<>(Collections.singleton(config.getProxy()));

        return TOOLS.getClientIp(request, proxyList);
    }


    public ClientVerification verifySubscriber(OtpAuth otp, String guid, HttpServletRequest request) throws UnauthorizedUserException, ResourceNotFoundException, ValidationException {
        Optional<Subscriptions> subscriber = subscriptionRepository.findByUuid(guid);
        if (subscriber.isEmpty()) throw new UnauthorizedUserException("failed_to_authenticate");
        Client matricul = clientMatriculRepository.findById(subscriber.get().getClientMatricul()).orElseThrow(() -> new UsernameNotFoundException("failed_to_authenticate ."));

        subscriber.get().setClient(matricul);

        InstitutionConfig config = institutionConfigService.findByyApp(Application.MB.name());
        ClientVerification verified = otpService.VerifyOtp(otp, guid, subscriber.get(), ip(request));
        Subscriptions subscription = subscriber.get();
        if (config.getSubMethod().equals(SubMethod.AUTOMATIC)) {
            SubscriptionDao dao = new SubscriptionDao();
            dao.setApplyFee(1);
            dao.setAccountId(subscriber.get().getPrimaryAccount());
            dao.setPackageCode(config.getDefaultPackage());

            subscription.setContactVerification(true);
            handleSubscriptionValidation(dao, "self", subscriber.get());

        } else if (config.getSubMethod().equals(SubMethod.MANUAL)) {
            subscription.setContactVerification(true);
        }

        subscriptionRepository.save(subscriber.get());
        return verified;
    }

//    public ClientVerification authorizeNewDevice(OtpAuth otp, String guid, String deviceUuid,HttpServletRequest request) throws UnauthorizedUserException, ResourceNotFoundException {
//        Optional<Subscriptions> client = subscriptionRepository.findByUuid(guid);
//        if (client.isEmpty()) throw new UnauthorizedUserException("failed_login");
//
//        Optional<ClientDevice> device = userDeviceRepository.findUserDeviceByUuidAndSubscriber(deviceUuid, client.get());
//
//        if (device.isEmpty()) throw new UnauthorizedUserException("failed_login");
//
//        ClientVerification verified = otpService.VerifyOtp(otp, guid, client.get(),ip(request));
//        device.get().setStatus("AUTHORIZED");
//        userDeviceRepository.save(device.get());
//        return verified;
//    }


    @Transactional
    public String resetPassword(String guid, ForgotPasswordDto pass) throws ValidationException {

        boolean passwordMatch = pass.getNewPassword().matches(pass.getConfirmPassword());
        PasswordConstraintValidator.isAcceptablePassword(pass.getNewPassword());

        if (!passwordMatch) {
            throw new ValidationException("Password does not match.");
        }
        Optional<Subscriptions> subscriber = subscriptionRepository.findByUuid(guid);
        if (subscriber.isEmpty()) {
            throw new ValidationException("User Not Found");
        }
        if (!Objects.equals(subscriber.get().getPasswordResetRequest(), "ALLOWED")) {
            throw new ValidationException("UNAUTHORIZED");
        }

        boolean isOldPassword = this.isPasswordReused(subscriber.get(), pass.getConfirmPassword());

        if (isOldPassword) throw new ValidationException("new password cannot be same as old password");

        subscriber.get().setPassword(passwordEncoder.encode(pass.getConfirmPassword()));
        subscriber.get().setPasswordResetRequest(null);
        subscriber.get().setPasswordChangedTime(LocalDateTime.now());
        subscriptionRepository.save(subscriber.get());

        AuditClientPassword passwordHistory = new AuditClientPassword();
        passwordHistory.setPassword(passwordEncoder.encode(pass.getConfirmPassword()));
        passwordHistory.setSubscriber(subscriber.get());
        this.auditClientPasswordRepository.save(passwordHistory);


        return "success";
    }


    public ClientVerification verifyResetPassRequest(String guid, OtpAuth otp, HttpServletRequest request) throws  UnauthorizedUserException {
        Optional<Subscriptions> user = subscriptionRepository.findByUuid(guid);
        if (user.isEmpty()) {
            throw new UnauthorizedUserException("UNAUTHORIZED1");
        }
        if (!Objects.equals(user.get().getPasswordResetRequest(), "INITIATED")) {
            throw new UnauthorizedUserException("UNAUTHORIZED2");
        }
        ClientVerification verified = otpService.VerifyOtp(otp, guid, user.get(), ip(request));

        user.get().setPasswordResetRequest("ALLOWED");
        subscriptionRepository.save(user.get());
        return verified;
    }

    public static boolean validateClientLogin(String input) throws ValidationException {
        // Check if the string is less than 8 characters
        if (input.length() < 5) {
            throw new ValidationException("username cannot be less than 5 characters");
        }

        // Check if the string contains spaces
        if (input.contains(" ")) {
            throw new ValidationException("username cannot have spaces");
        }

        // Check if the string contains special characters
        // Define a pattern to match special characters
        Pattern specialCharPattern = Pattern.compile("[^a-zA-Z0-9 ]");
        if (specialCharPattern.matcher(input).find()) {
            throw new ValidationException("username cannot have special characters");
        }

        // If all conditions are met, return true
        return true;
    }

    public boolean isPasswordReused(Subscriptions subscriber, String newPassword) {
        List<AuditClientPassword> previousPasswords = auditClientPasswordRepository.findBySubscriber(subscriber);

        for (AuditClientPassword oldHashedPassword : previousPasswords) {
            if (passwordEncoder.matches(newPassword, oldHashedPassword.getPassword())) {
                return true; // Password is reused
            }
        }
        return false; // Password is unique
    }


}
