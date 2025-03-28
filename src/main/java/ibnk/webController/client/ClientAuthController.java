package ibnk.webController.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import ibnk.dto.BankingDto.AccountMvtDto;
import ibnk.dto.BankingDto.TransferModel.AccountCallback;
import ibnk.dto.BankingDto.TransferModel.MobilePayment;
import ibnk.dto.BankingDto.TransferModel.Transaction;
import ibnk.dto.BankingDto.TransferModel.TransactionData;
import ibnk.dto.ClientRequestDto;
import ibnk.dto.SubscriptionDao;
import ibnk.dto.UserDto;
import ibnk.dto.auth.*;
import ibnk.intergrations.BetaSms.BetaSmsService;
import ibnk.intergrations.Tranzak.TranzakService;
import ibnk.intergrations.Tranzak.requestDtos.PaymentCallbackDto;

import ibnk.models.internet.InstitutionConfig;
import ibnk.models.internet.Media;
import ibnk.models.internet.TermAndCondition;
import ibnk.models.internet.client.ClientRequest;
import ibnk.models.internet.client.Subscriptions;
import ibnk.models.internet.enums.OtpEnum;
import ibnk.models.internet.enums.Status;
import ibnk.repositories.rptBanking.RptLogoRepository;
import ibnk.service.*;
import ibnk.service.BankingService.MobilePaymentService;
import ibnk.tools.Interceptors.InterceptPin;
import ibnk.tools.Interceptors.InterceptQuestions;
import ibnk.tools.ResponseHandler;
import ibnk.tools.TOOLS;
import ibnk.tools.error.*;
import ibnk.intergrations.EmailService;
import ibnk.tools.response.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/api/v1/client")
public class ClientAuthController {
    private final CustomerService customerService;
    private final OtpService otpService;
    private final MobilePaymentService mobilePaymentService;
    private final ClientRequestService clientRequestService;
    private final TranzakService tranzakService;
    private final AtomicBoolean operationCompleted = new AtomicBoolean(false);
    private final InstitutionConfigService institutionConfigService;
    private final MediaService mediaService;
    private final RptLogoRepository rptLogoRepository;
    private final BetaSmsService betaSmsService;

    private final EmailService emailService;
    public String cookieValue = "user-device-cookie";

    @Value("${spring.tranzak.enviroment}")
    String ENV;
    @Value("${external.transaction.x-hash}")
    String X_HASH;

    @Value("${spring.transak.auth}")
    String AUTH;


    @PostMapping("/auth/authenticate")
    public ResponseEntity<Object> authentication(HttpServletRequest request,
                                                 @Valid @RequestBody AuthDto auth,
                                                 @CookieValue(value = "user-device-cookie", required = false) String deviceCookie,
                                                 @RequestHeader(value = "User-Agent") String device) throws UnauthorizedUserException, JsonProcessingException {
        AuthResponse<Object, Object> resource = customerService.authenticate(auth, request);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", resource);
    }

    @GetMapping("auth/sub-details/{contact}")
    public ResponseEntity<Object> getSubClient(@PathVariable String contact) throws ResourceNotFoundException {
        UserDto.CreateSubscriberClientDto result = customerService.ClientDetails(contact);
        return ResponseHandler.generateResponse(HttpStatus.OK, false, "Success", result);
    }

    @PostMapping("auth/subscribe")
    public ResponseEntity<Object> subscriptions(@RequestBody SubscriptionDao dao) throws SQLException, ResourceNotFoundException, ValidationException, UnauthorizedUserException, OtpSubscriberException {
        InstitutionConfig config = institutionConfigService.getInstConfig();
        AuthResponse<UserDto.CreateSubscriberClientDto, CustomerVerification> cus = customerService.SelfSubscription(dao, config);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", cus);
    }

    @CrossOrigin
    @GetMapping("/auth/forgot/password/{login}")
    public ResponseEntity<Object> ForgotUserPassword(@PathVariable String login) throws UnauthorizedUserException, ResourceNotFoundException, ibnk.tools.error.ValidationException {
        var otp = customerService.forgotPassword(login);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", otp);
    }
    @CrossOrigin
    @PostMapping("/auth/set-pin/{uuid}")
    public ResponseEntity<Object> setPinUser(@PathVariable(name = "uuid") String verificationUuid,@RequestBody ForgotPasswordDto.PinDto dto,HttpServletRequest request) throws UnauthorizedUserException, ibnk.tools.error.ValidationException, JsonProcessingException {
        var res = customerService.setPin( verificationUuid, dto,request);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", res);
    }

    @CrossOrigin
    @PostMapping("/auth/set-password/{uuid}")
    public ResponseEntity<Object> setUserPassword(@PathVariable(name = "uuid") String verificationUuid,@RequestBody ForgotPasswordDto dto, HttpServletRequest request) throws ibnk.tools.error.ValidationException, JsonProcessingException {
        customerService.setPassword( verificationUuid, dto, request);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "password set successfully", null);
    }



    @PostMapping("/auth/verifyOtp/{guid}")
    public ResponseEntity<Object> VerifyOtpResetPasswordAndFirstLogin(HttpServletRequest request, @PathVariable(value = "guid") String guid, @RequestBody OtpAuth otp) throws ResourceNotFoundException, UnauthorizedUserException,  ExpiredPasswordException, ibnk.tools.error.ValidationException {
        Object result = new Object();
        switch (OtpEnum.valueOf(otp.getRole())) {
            case RESET_PASSWORD -> {
                result = customerService.verifyResetPassRequest(guid, otp,request);
            }
            case FIRST_LOGIN , RESET_PIN -> {
                result = customerService.verifyFirstLogin(otp, guid,request);
            }
            case OPEN_ACCOUNT_REQUEST -> {
                result = customerService.VerifyAccountRequest(otp, guid);
            }
            case DOUBLE_AUTHENTICATION -> {
                result = customerService.OauthWithOtp(guid, otp,request);
            }
            case SUBSCRIPTION_REQUEST -> {
                result = customerService.verifySubscriber(otp, guid,request);
            }
//            case NEW_DEVICE_LOGIN -> {
//                result = customerService.authorizeNewDevice(otp, guid, deviceCookie,request);
//            }
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", result);
    }

    @GetMapping("/auth/resend-otp/{uuid}/{guid}")
    public ResponseEntity<Object> ResendOtp(@PathVariable(value = "uuid") String uuid, @PathVariable String guid) throws Exception {
        Subscriptions client = customerService.findClientByUuid(guid);

        UserDto.CreateSubscriberClientDto clientDto = UserDto.CreateSubscriberClientDto.modelToDao(client);

        CustomerVerification result = otpService.GenerateResendOtp(uuid, client);

        AuthResponse<UserDto.CreateSubscriberClientDto, CustomerVerification> res = new AuthResponse<UserDto.CreateSubscriberClientDto, CustomerVerification>(clientDto, result);

        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", res);
    }

    @PostMapping("auth/reset-password/{guid}")
    public ResponseEntity<Object> ResetPassword(@PathVariable(value = "guid") String guid, @RequestBody ForgotPasswordDto pass) throws ValidationException {
        var otp = customerService.resetPassword(guid, pass);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", otp);
    }

    @InterceptQuestions
    @PutMapping("/update-profile")
    public ResponseEntity<Object> updateProfile(@RequestBody UserDto.CreateSubscriberClientDto dto, @AuthenticationPrincipal Subscriptions subs) throws ResourceNotFoundException, UnauthorizedUserException {
        var response = customerService.updateClientProfile(dto, subs);
        return ResponseHandler.generateResponse(HttpStatus.OK, false, "success", response);
    }

    @InterceptQuestions
    @PutMapping("/update-primary-account")
    public ResponseEntity<Object> updatePrimaryAccount(@RequestBody UserDto.CreateSubscriberClientDto dto, @AuthenticationPrincipal Subscriptions subs) throws ResourceNotFoundException {
        var response = customerService.updatePrimaryAccount(dto, subs);
        return ResponseHandler.generateResponse(HttpStatus.OK, false, "success", response);
    }

    @InterceptPin
    @PutMapping("/update-password")
    public ResponseEntity<Object> updatePassword(@AuthenticationPrincipal Subscriptions subscriptions,HttpServletRequest request ,@RequestBody UpdatePasswordDto dto) throws ValidationException, UnauthorizedUserException {
        var response = customerService.UpdatePassword(subscriptions, dto,request);
        return ResponseHandler.generateResponse(HttpStatus.OK, false, "success", response);
    }

    @PutMapping("/update-pin")
    public ResponseEntity<Object> updatePin(@AuthenticationPrincipal Subscriptions subscriptions,HttpServletRequest request, @RequestBody UpdatePasswordDto.UpdatePinDto dto) throws ValidationException, UnauthorizedUserException {
        var response = customerService.UpdatePin(subscriptions, dto,request);
        return ResponseHandler.generateResponse(HttpStatus.OK, false, "success", response);
    }

    @PostMapping("auth/diaspora-deposit/call-back")
    public ResponseEntity<Object> diasporaDepositCallBack(@RequestBody PaymentCallbackDto dto, @AuthenticationPrincipal Subscriptions subscriber) throws Exception {
        // Process the payment callback
        // Extract relevant information and perform necessary actions
        if (dto.getAuthKey().equalsIgnoreCase(AUTH)) {
            JSONObject jsonObject = new JSONObject();
            String requestId = dto.getResource().getRequestId();
            String status = dto.getResource().getStatus();
            if (dto.getEventType().equals("REQUEST.COMPLETED")) {
                MobilePayment pay = mobilePaymentService.get_paymentUuid(dto.getResource().getRequestId())
                        .stream()
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("Request Does not Exist"));

                if (pay.getStatus().trim().equals("PENDING")) {
                    pay.setCallBackReceive(true);
                    pay.setTrxNumber(dto.getResource().getTransactionId());
                    mobilePaymentService.updateMobilePayment(pay);
                    System.out.println("Received payment callback for request ID: " + requestId + " with status: " + status);
                    if (status.equals("SUCCESSFUL")) {
                        if (ENV.equals("PRODUCTION")) {

                            AccountMvtDto transfer = new AccountMvtDto();
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
                            String date = simpleDateFormat.format(new Date());
                            transfer.setAccountId(pay.getCpteJumelle());
                            transfer.setPhoneNumber(pay.getTelephone());
                            transfer.setAmount(pay.getMontant());
                            transfer.setDate(date);
                            transfer.setIds("");
                            transfer.setTypeOp(pay.getTypeOperation());
                            transfer.setSens(1);
                            transfer.setDescription(dto.getResource().getDescription());

                            AccountMvtDto item = mobilePaymentService.account_mvt(transfer);
//                            AccountBalanceDto.AccountDebitDto debit = accountService.CreditDiaspora(pay);
                            System.out.println("GLOBAL BANK DEBIT:" + item.getPc_OutMSG());
                            pay.TrxNumber = item.getPc_OutID();
                            if (item.getPc_OutLECT() != 0) {
                                operationCompleted.set(true);
                                return ResponseHandler.generateResponse(HttpStatus.UNPROCESSABLE_ENTITY, false, item.getPc_OutMSG(), item.getPc_OutMSG());
                            }
                            pay.setStatus("SUCCESS");
                            mobilePaymentService.updateMobilePayment(pay);
                            return ResponseHandler.generateResponse(HttpStatus.OK, false, "success", pay.getStatus());
                        }
                        pay.setStatus("SUCCESS");
                        mobilePaymentService.updateMobilePayment(pay);
                        return ResponseHandler.generateResponse(HttpStatus.OK, false, "success", pay.getStatus());
                    }
                    pay.setStatus(status.trim());
                    mobilePaymentService.updateMobilePayment(pay);
                    return ResponseHandler.generateResponse(HttpStatus.OK, false, "success", jsonObject.get("message"));
//
                }
                return ResponseHandler.generateResponse(HttpStatus.OK, false, "success", "INVALID PAYMENT");
            }

            return ResponseHandler.generateResponse(HttpStatus.OK, false, status.trim(), "Request INCOMPLETE");
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, false, "unauthorized", "Request INCOMPLETE");
    }

    @PostMapping("auth/callBack/payment")
    public ResponseEntity<Object> call_back(@RequestBody TransactionData transactionData, @RequestHeader("x-hash") String key) throws Exception {
        if (!key.equalsIgnoreCase(X_HASH)) {
            throw new UnauthorizedUserException("Unauthorised");
        }

        Transaction transaction = transactionData.getTransaction();
        String status = String.valueOf(transaction.getStatus());
        if (transaction.getUuid() == null) {
            return ResponseHandler.generateResponse(HttpStatus.OK, false, "INVALID REQUEST", "INVALID REQUEST");
        }
        MobilePayment pay = mobilePaymentService.get_paymentUuid(transaction.getUuid())
                .stream()
                .findFirst()
                .orElseThrow();

        if (pay.getClient() == null) {
            return ResponseHandler.generateResponse(HttpStatus.OK, false, "PAYMENT NOT FOUND", "PAYMENT NOT FOUND");

        }
        if (pay.getStatus().trim().equals("FAILED") || pay.getStatus().trim().equals("CANCELLED")) {
            // set call to true
            return ResponseHandler.generateResponse(HttpStatus.OK, false, "FAILED PAYMENT STATUS", "FAILED PAYMENT STATUS");
        }
        if(pay.getStatus().trim().equals("SUCCESS")){
            return ResponseHandler.generateResponse(HttpStatus.OK, false, "PAYMENT COMPLETED", "PAYMENT COMPLETED");
        }
        
        if (pay.getCallBackReceive()) {
            return ResponseHandler.generateResponse(HttpStatus.OK, false, "INVALID CALL BACK", "INVALID PAYMENT");
        }
        pay.setCallBackReceive(true);
        pay.setStatus(status);


        if ((status.trim().equals("FAILED") || status.trim().equals("CANCELLED")) && pay.Type.trim().equals("WITHDRAWAL")) {
            AccountCallback repay = new AccountCallback();
            repay.setAccount(pay.getCpteJumelle());
            repay.setTrxNumber(pay.getTrxNumber());
            repay.setTelephone(pay.getTelephone());

            mobilePaymentService.account_callback(repay);
            operationCompleted.set(true);
            pay.setCallBackReceive(true);
            mobilePaymentService.updateMobilePayment(pay);

            return ResponseHandler.generateResponse(HttpStatus.OK, false, "PAYMENT REFUNDED", "PAYMENT REFUNDED");

        } else if (status.trim().equals("SUCCESS") && pay.Type.trim().equals("DEPOSIT")) {
            AccountMvtDto transfer = new AccountMvtDto();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String date = simpleDateFormat.format(new Date());
            transfer.setAccountId(pay.getCpteJumelle());
            transfer.setPhoneNumber(pay.getTelephone());
            transfer.setAmount(pay.getMontant());
            transfer.setDate(date);
            transfer.setIds("");
            transfer.setTypeOp(pay.getTypeOperation());

            transfer.setSens(1);
            transfer.setDescription(
                    transactionData.getTransaction()
                            .getPayment_method().getName().concat(" Deposit from ").
                            concat(transactionData.getTransaction().getRecipient()));

            AccountMvtDto item = mobilePaymentService.account_mvt(transfer);
            pay.TrxNumber = item.getPc_OutID();
            if (item.getPc_OutLECT() != 0) {
                operationCompleted.set(true);
                return ResponseHandler.generateResponse(HttpStatus.UNPROCESSABLE_ENTITY, false, item.getPc_OutMSG(), item.getPc_OutMSG());
            }
            mobilePaymentService.updateMobilePayment(pay);
            operationCompleted.set(true);

            return ResponseHandler.generateResponse(HttpStatus.OK, false, "PAYMENT COMPLETED", "PAYMENT COMPLETED");
        } else {
            mobilePaymentService.updateMobilePayment(pay);
            operationCompleted.set(true);

            return ResponseHandler.generateResponse(HttpStatus.OK, false, "PAYMENT UPDATED", "OK");

        }


    }

    @PostMapping("auth/account-request")
    public ResponseEntity<Object> savePhysicalClient(@Valid @RequestBody ClientRequestDto.BasicRequestDto clientRequestDto) throws ResourceNotFoundException {
        boolean result = clientRequestService.initiateAccountRequest(clientRequestDto);
        return ResponseHandler.generateResponse(HttpStatus.OK, false, "Success", result);
    }

    @GetMapping("auth/verify-account-request/{uuid}")
    public ResponseEntity<Object> verifyAccountRequest(@PathVariable("uuid") String uuid) throws ValidationException {
        Optional<ClientRequest> accountRequest = clientRequestService.findByUuid(uuid);
        if (accountRequest.isEmpty()) {
            throw new ValidationException("account_request_invalid");
        }
        ClientRequest clientRequest = accountRequest.get();

        if (clientRequest.getStatus().equals(Status.PENDING.toString())) {
            throw new ValidationException("this_link_is_no_longer_valid");
        }

        return ResponseHandler.generateResponse(HttpStatus.OK, false, "Success", accountRequest.get());
    }

    @PostMapping("auth/account-identification/{uuid}")
    public ResponseEntity<Object> updatePhysicalClient(@PathVariable("uuid") String uuid, @Valid @RequestBody ClientRequestDto.IdentificationDto dto) throws ResourceNotFoundException, ValidationException {
        String data = clientRequestService.updateAccountRequestIdentification(uuid, dto);
        return ResponseHandler.generateResponse(HttpStatus.OK, false, "Success", data);
    }

    @PostMapping("/auth/upload-media/{guid}")
    public ResponseEntity<Object> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("role") String role,
            @PathVariable String guid) throws IOException {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is missing");
        }
        File convertedFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error saving file");
        }
        Media media = new Media();
        media.setFileName(file.getOriginalFilename());
        media.setRole(role);
        media.setGuid(guid);
        media.setSize(file.getSize());
        media.setImg(Files.readAllBytes(convertedFile.toPath()));
        media.setType(ibnk.models.internet.enums.MediaType.IMAGE.name());
        media.setOriginalFileName(file.getOriginalFilename());
        media.setPhoto(convertedFile.toPath().toString());
        media.setExtension(TOOLS.getFileExtension(file.getOriginalFilename()));
        boolean error = false;
        Media mediaDetails = mediaService.save(media);
        return ResponseHandler.generateResponse(HttpStatus.OK, error, "success", mediaDetails.getUuid());
    }

    @GetMapping("auth/find-term/{code}")
    public ResponseEntity<Object> findTermByCode(@PathVariable(value = "code") String code) throws ResourceNotFoundException {
        TermAndCondition response = institutionConfigService.findTermAndConditionByCode(code);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", response);
    }
}