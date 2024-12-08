package ibnk.webController.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import ibnk.dto.BankingDto.*;
import ibnk.dto.BankingDto.TransferModel.*;
import ibnk.dto.NotificationEvent;
import ibnk.dto.UserDto;
import ibnk.dto.auth.CustomerVerification;
import ibnk.intergrations.Tranzak.TranzakService;
import ibnk.intergrations.Tranzak.requestDtos.InitiateCollection;
import ibnk.intergrations.Tranzak.requestDtos.PhoneVerification;
import ibnk.intergrations.Tranzak.responseDtos.InitiateCollectionResponse;
import ibnk.intergrations.Tranzak.responseDtos.PhoneVerifyResponse;
import ibnk.models.internet.InstitutionConfig;
import ibnk.models.internet.OtpEntity;
import ibnk.models.internet.client.Subscriptions;
import ibnk.models.internet.enums.*;
import ibnk.repositories.internet.InstitutionConfigRepository;
import ibnk.service.BankingService.AccountService;
import ibnk.service.BankingService.MobilePaymentService;
import ibnk.service.BankingService.PaymentService;
import ibnk.service.OtpService;
import ibnk.tools.Interceptors.InterceptPin;
import ibnk.tools.Interceptors.InterceptQuestions;
import ibnk.tools.ResponseHandler;
import ibnk.tools.error.ResourceNotFoundException;
import ibnk.tools.error.UnauthorizedUserException;
import ibnk.tools.error.ValidationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.json.JSONObject;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/api/v1/client/transactions")
public class TransactionController {
    private final AccountService accountService;
    private final MobilePaymentService mobilePaymentService;
    private final PaymentService paymentService;
    private final OtpService otpService;
    private final TranzakService tranzakService;
    private final InstitutionConfigRepository institutionConfigRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

//    @InterceptQuestions
    @InterceptPin
    @PostMapping("initiate-internal-transfer")
    public ResponseEntity<Object> makeTransfer(@RequestBody AccountTransferDto dao, @AuthenticationPrincipal Subscriptions subscription) throws UnauthorizedUserException, ResourceNotFoundException, ValidationException {
        MobilePayment initiatedPayment;
        initiatedPayment = MobilePayment.AccountTransferDtoToMobilePayDeposit(dao, subscription);
        Optional<InstitutionConfig> config = institutionConfigRepository.findByApplication(Application.MB.name());
        if(config.isEmpty()){
            throw new ValidationException("Configure Settings");
        }
        if(config.get().isTrnasOtp()){
            initiatedPayment.setStatus("INITIATED");
            mobilePaymentService.insertionMobilePayment(initiatedPayment);
            List<Object> payload = new ArrayList<>();
            OtpEntity params = OtpEntity.builder()
                    .guid(initiatedPayment.getUuid())
                    .email(subscription.getEmail())
                    .phoneNumber(subscription.getPhoneNumber())
                    .role(OtpEnum.VALIDATE_INTERNAL_TRANSACTION)
                    .transport(subscription.getPreferedNotificationChanel())
                    .build();
            payload.add(UserDto.CreateSubscriberClientDto.modelToDao(subscription));
            payload.add(dao);
            CustomerVerification verificationObject = otpService.GenerateAndSend(params, payload, subscription);
            return ResponseHandler.generateResponse(HttpStatus.OK, true, "success", verificationObject);

        }
        else {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            AccountTransferDto trans = new AccountTransferDto();
            AccountTransferDto transi = new AccountTransferDto();

            try {
//            Used  getPaymentGatewaysUuid to get Memo since MobilePayment does not have a column called description
                trans.setMemo(initiatedPayment.getPaymentGatewaysUuid());
                trans.setAmount(initiatedPayment.getMontant());
                trans.setBeneficiary_account(initiatedPayment.getBenefAccount());
                trans.setAccountId(initiatedPayment.getCpteJumelle());
                trans.setClient(initiatedPayment.getClient());
                trans.setIds(initiatedPayment.getTrxNumber());

                trans = accountService.account_transfer(trans, subscription);
                transi.setMemo(initiatedPayment.getPaymentGatewaysUuid());
                transi.setAmount(initiatedPayment.getMontant());
                transi.setBeneficiary_account(initiatedPayment.getBenefAccount());
                transi.setAccountId(initiatedPayment.getCpteJumelle());
                transi.setClient(initiatedPayment.getClient());
                transi.setMessage(trans.getPc_OutMSG());
                transi.setStatus("SUCCESS");
                transi.setIds(initiatedPayment.getTrxNumber());
                transi.setId_transaction(trans.getId_transaction());
                transi.setFee(trans.getFee());
                transi.setTax(trans.getTax());
                transi.setDate(LocalDate.now().format(format));
                List<Object> payload = new ArrayList<>();
                payload.add(transi);
                payload.add(UserDto.CreateSubscriberClientDto.modelToDao(subscription));
                NotificationEvent event = new NotificationEvent();
                event.setEventCode(EventCode.ACCOUNT_TRANSFER.name());
                event.setPayload(payload);
                event.setType(subscription.getPreferedNotificationChanel());
                event.setPhoneNumber(subscription.getPhoneNumber());
                event.setEmail(subscription.getEmail());
                event.setSubscriber(subscription);
                applicationEventPublisher.publishEvent(event);
                trans.setStatus("SUCCESS");
                trans.setMessage("SUCCESS");
            } catch (Exception e) {
                e.printStackTrace();
                throw new ValidationException(e.getMessage());
            }
            return ResponseHandler.generateResponse(HttpStatus.OK, false, "success", transi);

        }


    }

//    @InterceptQuestions
    @PostMapping("initiate/{type}")
    @InterceptPin
    public ResponseEntity<Object> inititateBankTransaction(@RequestBody AccountMvtDto dto, @AuthenticationPrincipal Subscriptions subscriber, @PathVariable String type) throws ResourceNotFoundException, UnauthorizedUserException, ValidationException, SQLException {
        MobilePayment initiatedPayment;
        MobilePayment mobilePayment;

        AccountEntityDto accountInfo = accountService.findClientAccounts(dto.getAccountId()).stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("invalid_account"));

        if( !Objects.equals(accountInfo.getClient(), subscriber.getClientMatricul())) {
            throw new ResourceNotFoundException("invalid_account");
        }

        if (type.equals("withdrawal")) {
            mobilePayment = MobilePayment.AccountMvtToMobilePayWithdraw(dto, subscriber);
            mobilePayment.setStatus("INITIATED");

            switch (TypeOperations.valueOf(mobilePayment.getTypeOperation())) {
                case  MTNCRE,MOMODE,NECRED,OMDEPO,ORCRED -> {
                    accountService.mobileLimit(dto,subscriber);
                }
                default -> {
                }
            }

            initiatedPayment = mobilePaymentService.insertionMobilePayment(mobilePayment);
            OtpEntity params = OtpEntity.builder()
                    .guid(initiatedPayment.getUuid())
                    .email(subscriber.getEmail())
                    .phoneNumber(subscriber.getPhoneNumber())
                    .role(OtpEnum.VALIDATE_TRANSACTION)
                    .transport(subscriber.getPreferedNotificationChanel())
                    .build();

            initiatedPayment.setName(accountInfo.getClientName());
            initiatedPayment.setAccountType(accountInfo.getAccountName());

            List<Object> payloads = new ArrayList<>();
            payloads.add(initiatedPayment);
            payloads.add(UserDto.CreateSubscriberClientDto.modelToDao(subscriber));
            CustomerVerification verificationObject = otpService.GenerateAndSend(params, payloads, subscriber);

            return ResponseHandler.generateResponse(HttpStatus.OK, false, "success", verificationObject);

        }
        else if (type.equals("deposit")) {

            initiatedPayment = MobilePayment.AccountMvtToMobilePayDeposit(dto, subscriber);

            ChannelCode channelCode;
            switch (TypeOperations.valueOf(initiatedPayment.getTypeOperation())) {
                case MTNRET -> channelCode = ChannelCode.CHANNEL_MTN_CM;
                case OMRETR -> channelCode = ChannelCode.CHANNEL_OM_CM;
                default -> {
                    throw new ResourceNotFoundException("INVALID  OPERATION TYPE");
                }
            }
            InitPayment initPayment = InitPayment.AccountMvnToInitPay(initiatedPayment, subscriber);

            initPayment.setType(PaymentType.cash_collect);
            initPayment.setChannel(channelCode);
            initPayment.setReference(initiatedPayment.getUuid());

            PaymentDto result = paymentService.initiatePayment(initPayment);
            initiatedPayment.setStatus("PENDING");
            mobilePaymentService.insertionMobilePayment(initiatedPayment);

            //                FIN ESSAI CALLBACK
            ExecutePayment execute = new ExecutePayment();
            execute.setSchema_type("CM_MOBILE_MONEY_SCHEMA");
            MomoSchemaType schema = new MomoSchemaType();
            schema.setPhoneNumber("237" + initiatedPayment.getTelephone());
            execute.setSchema(schema);

            PaymentDto executeResponse = paymentService.executePayment(execute, result.getData().getTransaction().getUuid());
            String status = executeResponse.getData().getTransaction().getStatus().toString();
            initiatedPayment.setCallBackReceive(false);

            initiatedPayment.setPaymentGatewaysUuid(executeResponse.getData().getTransaction().getUuid());

            mobilePaymentService.updateMobilePayment(initiatedPayment);

            if (status.trim().equals("PENDING") || status.trim().equals("WAITING_FOR_PAYMENT")) {
                return ResponseHandler.generateResponse(HttpStatus.OK, false, "pending", initiatedPayment);
            }

            return ResponseHandler.generateResponse(HttpStatus.OK, false, status.trim(), initiatedPayment);


        }
        else {
            throw new ValidationException("invalid_operation");
        }

    }

//    @InterceptQuestions
//    @PostMapping("diaspora-deposit")
//    public ResponseEntity<Object> diasporaDeposit(@RequestBody AccountMvtDto dto, @AuthenticationPrincipal Subscriptions subscriber) throws ResourceNotFoundException, JsonProcessingException, ValidationException {
//        dto.setTypeOp("MOMODE");
//        MobilePayment initiatedPayment = MobilePayment.AccountMvtToMobilePayDeposit(dto, subscriber);
//        initiatedPayment.setUuid(generateUniqueReference());
//        mobilePaymentService.insertionMobilePayment(initiatedPayment);
//
//        InitiateCollection collectionDto = new InitiateCollection();
//        collectionDto.setDescription(dto.getDescription());
//        collectionDto.setAmount(String.valueOf(dto.getAmount()));
//        collectionDto.setCurrencyCode(dto.getCurrency());
//        collectionDto.setMchTransactionRef(initiatedPayment.getUuid());
//        collectionDto.setReturnUrl("");
//        InitiateCollectionResponse response = null;
//
//        try {
//            response = tranzakService.generateRedirectPayment(collectionDto, subscriber);
//            initiatedPayment.setPaymentGatewaysUuid(response.getData().getRequestId());
//            initiatedPayment.setTrxNumber(response.getData().getMchTransactionRef().substring(1, 10));
//            mobilePaymentService.updateMobilePayment(initiatedPayment);
//
//        } catch (Exception e) {
//            initiatedPayment.setStatus("FAILED");
//            mobilePaymentService.updateMobilePayment(initiatedPayment);
//            e.printStackTrace();
//            throw e;
//        }
//        return ResponseHandler.generateResponse(HttpStatus.OK, false, "success", response);
//    }

    // TODO Test "diaspora-deposit/callBack"
//    @PostMapping("diaspora-deposit/callBack")
//    public ResponseEntity<Object> diasporaDepositCallBack(@RequestBody PaymentCallbackDto dto, @AuthenticationPrincipal Subscriptions subscriber) throws Exception {
//        // Process the payment callback
//        // Extract relevant information and perform necessary actions
//        JSONObject jsonObject = new JSONObject();
//        String requestId = dto.getResource().getRequestId();
//        String status = dto.getResource().getStatus();
//
//        MobilePayment pay = mobilePaymentService.get_paymentUuid(dto.getResource().getRequestId())
//                .stream()
//                .findFirst()
//                .orElseThrow(() -> new ResourceNotFoundException("Request Does not Exist"));
//
//        if (pay.getStatus().equals("PENDING") && !pay.getCallBackReceive()) {
//            pay.setCallBackReceive(false);
//            pay.setTrxNumber(dto.getResource().getTransactionId());
//            mobilePaymentService.updateMobilePayment(pay);
//            System.out.println("Received payment callback for request ID: " + requestId + " with status: " + status);
//            if (status.equals("SUCCESSFUL")) {
//                // Example: Update database,
//                AccountBalanceDto.AccountDebitDto debit = accountService.CreditDiaspora(pay);
//                System.out.println("GLOBAL BANK DEBIT:" + debit.getPc_OutMSG());
//                if (debit.getPc_OutId() == 0) {
//                    pay.setStatus("SUCCESS");
//                    mobilePaymentService.updateMobilePayment(pay);
//                    return ResponseHandler.generateResponse(HttpStatus.OK, false, "success", pay);
//                }
//            }
//            if (pay.getStatus().trim().equals("FAILED")) {
//                jsonObject.put("status", 0);
//                jsonObject.put("message", "INVALID PAYMENT STATUS");
//                pay.setStatus("FAILED");
//                mobilePaymentService.updateMobilePayment(pay);
//                return ResponseHandler.generateResponse(HttpStatus.OK, false, "success", jsonObject);
//            }
//        }
//
//        if (pay.getCallBackReceive()) {
//            jsonObject.put("status", 0);
//            jsonObject.put("message", "INVALID PAYMENT");
//            return ResponseHandler.generateResponse(HttpStatus.OK, false, "success", jsonObject);
//        }
//        mobilePaymentService.updateMobilePayment(pay);
//        return ResponseHandler.generateResponse(HttpStatus.OK, false, status.trim(), pay);
//
//    }


    public ResponseEntity<Object> validatedInitiatedOperation(Subscriptions subscriber, String operationUuid) throws Exception {
        MobilePayment initiatedPayment = mobilePaymentService.getPaymentBytUuidAndClient(operationUuid, subscriber.getClientMatricul())
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("invalid_transaction"));

        if (!initiatedPayment.getStatus().trim().equals("INITIATED"))
            throw new ResourceNotFoundException("invalid_transaction");

        if (initiatedPayment.getType().equals("WITHDRAWAL")) {
            ChannelCode channelCode;
            switch (TypeOperations.valueOf(initiatedPayment.getTypeOperation())) {
                case MOMODE -> channelCode = ChannelCode.CHANNEL_MTN_CM;
                case OMDEPO -> channelCode = ChannelCode.CHANNEL_OM_CM;
                case MTNCRE -> channelCode = ChannelCode.CHANNEL_MTN_AIRTIME_CM;
                case ORCRED -> channelCode = ChannelCode.CHANNEL_ORANGE_AIRTIME_CM;
                case NECRED -> channelCode = ChannelCode.CHANNEL_NEXTTEL_AIRTIME_CM;
                case ENEOBI -> channelCode = ChannelCode.CHANNEL_ENEO_BILLS_CM;
                case WATERBI -> channelCode = ChannelCode.CHANNEL_CAMWATER_BILLS_CM;
                case CANALB -> channelCode = ChannelCode.CHANNEL_CANAL_PLUS_BILLS_CM;
                default -> {
                    throw new ResourceNotFoundException("INVALID  OPERATION TYPE");
                }
            }

            InitPayment initPayment = InitPayment.AccountMvnToInitPay(initiatedPayment, subscriber);

            initPayment.setType(PaymentType.payout);
            initPayment.setChannel(channelCode);
            initPayment.setReference(initiatedPayment.getUuid());
            PaymentDto result = null;
            try {
                result = paymentService.initiatePayment(initPayment);
            } catch (Exception e) {
                e.printStackTrace();
                throw new ValidationException("some thing went wrong try later");
            }
            initiatedPayment.PaymentGatewaysUuid = result.getData().getTransaction().getUuid();
            initiatedPayment.setStatus("PENDING");
            mobilePaymentService.updateMobilePayment(initiatedPayment);


            AccountMvtDto accountMovementDto = AccountMvtDto.builder()
                    .accountId(initiatedPayment.getCpteJumelle())
                    .phoneNumber(Integer.parseInt(initiatedPayment.getTelephone()))
                    .amount(initiatedPayment.getMontant())
                    .sens(2)
                    .typeOp(initiatedPayment.getTypeOperation())
                    .date(initiatedPayment.getDate())
                    .description(initiatedPayment.description)
                    .ids(initiatedPayment.getUuid()).build();

            AccountMvtDto accountMovement = mobilePaymentService.account_mvt(accountMovementDto);
            initiatedPayment.setTrxNumber(accountMovement.getPc_OutID());
            mobilePaymentService.updateMobilePayment(initiatedPayment);


            ExecutePayment execute = new ExecutePayment();
            execute.setSchema_type("CM_MOBILE_MONEY_SCHEMA");
            MomoSchemaType schema = new MomoSchemaType();
            schema.setPhoneNumber("237" + initiatedPayment.getTelephone());
            execute.setSchema(schema);

            PaymentDto executeResponse = null;

            try {
                executeResponse = paymentService.executePayment(execute, result.getData().getTransaction().getUuid());
            } catch (Exception e) {
                throw new ValidationException("some thing went wrong try later");
            }
            String status = executeResponse.getData().getTransaction().getStatus().toString();
            initiatedPayment.setMessage("Transaction " + status.trim());


            if (status.trim().equals("PENDING") || status.trim().equals("WAITING_FOR_PAYMENT")) {
                initiatedPayment.setCallBackReceive(false);
                initiatedPayment.setStatus("PENDING");
                mobilePaymentService.updateMobilePayment(initiatedPayment);
                return ResponseHandler.generateResponse(HttpStatus.OK, false, "pending", initiatedPayment);
            } else if (status.trim().equals("SUCCESS")) {
                initiatedPayment.setStatus("SUCCESS");
                initiatedPayment.setCallBackReceive(true);

                mobilePaymentService.updateMobilePayment(initiatedPayment);

                List<Object> payload = new ArrayList<>();
                payload.add(initiatedPayment);
                payload.add(UserDto.CreateSubscriberClientDto.modelToDao(subscriber));

                NotificationEvent event = new NotificationEvent();
                event.setEventCode(EventCode.WITHDRAWAL_COMPLETED.name());
                event.setPayload(payload);
                event.setType(subscriber.getPreferedNotificationChanel());
                event.setEmail(subscriber.getEmail());
                event.setPhoneNumber(subscriber.getPhoneNumber());
                event.setSubscriber(subscriber);
                applicationEventPublisher.publishEvent(event);

                return ResponseHandler.generateResponse(HttpStatus.OK, false, "success", initiatedPayment);
            }

            if (status.trim().equals("FAILED")) {

                AccountCallback accountCallback = new AccountCallback();
                accountCallback.setAccount(initiatedPayment.getCpteJumelle());
                accountCallback.setTrxNumber(initiatedPayment.getTrxNumber());
                accountCallback.setTelephone(initiatedPayment.getTelephone());

                mobilePaymentService.account_callback(accountCallback);

                initiatedPayment.setStatus(status.trim());
                initiatedPayment.setCallBackReceive(true);

                mobilePaymentService.updateMobilePayment(initiatedPayment);

                return ResponseHandler.generateResponse(HttpStatus.OK, false, "FAILED PAYMENT", initiatedPayment);
            }

        }

        throw new ResourceNotFoundException("invalid_transaction");
    }

    public ResponseEntity<Object> validateInitiatedTransfer(Subscriptions subscriber, String transferUuid) throws Exception {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        AccountTransferDto trans = new AccountTransferDto();
        AccountTransferDto transi = new AccountTransferDto();
        MobilePayment initiatedPayment = mobilePaymentService.getPaymentBytUuidAndClient(transferUuid, subscriber.getClientMatricul())
                .stream()
                .findFirst()
                .orElseThrow(() -> new ValidationException("invalid_transaction"));
        try {
//            Used  getPaymentGatewaysUuid to get Memo since MobilePayment does not have a column called description
            trans.setMemo(initiatedPayment.getPaymentGatewaysUuid());
            trans.setAmount(initiatedPayment.getMontant());
            trans.setBeneficiary_account(initiatedPayment.getBenefAccount());
            trans.setAccountId(initiatedPayment.getCpteJumelle());
            trans.setClient(initiatedPayment.getClient());
            trans.setIds(initiatedPayment.getTrxNumber());

            trans = accountService.account_transfer(trans, subscriber);
            transi.setMemo(initiatedPayment.getPaymentGatewaysUuid());
            transi.setAmount(initiatedPayment.getMontant());
            transi.setBeneficiary_account(initiatedPayment.getBenefAccount());
            transi.setAccountId(initiatedPayment.getCpteJumelle());
            transi.setClient(initiatedPayment.getClient());
            transi.setIds(initiatedPayment.getTrxNumber());
            transi.setId_transaction(trans.getId_transaction());
            transi.setStatus("SUCCESS");
            transi.setFee(trans.getFee());
            transi.setTax(trans.getTax());
            transi.setDate(LocalDate.now().format(format));
            List<Object> payload = new ArrayList<>();
            payload.add(transi);
            payload.add(UserDto.CreateSubscriberClientDto.modelToDao(subscriber));
            NotificationEvent event = new NotificationEvent();
            event.setEventCode(EventCode.ACCOUNT_TRANSFER.name());
            event.setPayload(payload);
            event.setType(subscriber.getPreferedNotificationChanel());
            event.setPhoneNumber(subscriber.getPhoneNumber());
            event.setEmail(subscriber.getEmail());
            event.setSubscriber(subscriber);
            applicationEventPublisher.publishEvent(event);
            trans.setStatus("SUCCESS");
            trans.setMessage("SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
            throw new ValidationException("some thing went wrong try later");
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, false, "success", transi);
    }


    @PostMapping("search-payable")
    public ResponseEntity<Object> SearchPayable(@RequestBody SearchDataBillDto bill) {
        ChannelCode channelCode;

        switch (bill.getTypeOp()) {
            case "ENEOBI" -> channelCode = ChannelCode.CHANNEL_ENEO_BILLS_CM;
            case "WATERBI" -> channelCode = ChannelCode.CHANNEL_CAMWATER_BILLS_CM;
            case "CANALB" -> channelCode = ChannelCode.CHANNEL_CANAL_PLUS_BILLS_CM;
            default -> {
                return ResponseHandler.generateResponse(HttpStatus.OK, false, "success", "INVALID TYPE OPERATION");
            }
        }

        PayableResponse response = paymentService.searchpayable(channelCode, bill.getId());
        return ResponseHandler.generateResponse(HttpStatus.OK, false, "success", response.getData().getItems());

    }

    @PostMapping(("billing-option"))
    public ResponseEntity<Object> BillingOptionVAT( @RequestBody() BillingListDto json, @AuthenticationPrincipal Subscriptions subscriptions) throws ValidationException, BadRequestException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        String date = simpleDateFormat.format(new Date());
        json.setPd_ServerDate(date);
        json.setLanguage("En");
        boolean isDebit;

        switch (TypeOperations.valueOf(json.getPc_TypeOp())) {
            case CANALB, WATERBI, ENEOBI, NECRED, ORCRED, MOMODE, MTNCRE, OMDEPO, OACTRF -> isDebit = true;
            default -> isDebit = false;
        }
        BillingListDto item = accountService.amountBillingOptionWithVAT(json, isDebit,subscriptions);
        return ResponseHandler.generateResponse(HttpStatus.OK, false, "success", item);
    }

    @GetMapping(("verify-name/{phoneNumber}"))
    public ResponseEntity<Object> VerifyMobilePhoneNumber(@PathVariable String phoneNumber) throws ValidationException, ResourceNotFoundException, JsonProcessingException, InterruptedException {
        PhoneVerification dto = new PhoneVerification();
        dto.setCustomTransactionId(String.valueOf(UUID.randomUUID()));
        dto.setAccountHolderId("+237" + phoneNumber);
        String clientName = "";

        PhoneVerifyResponse response = tranzakService.verifyCustomerPhoneNumberBetter(dto);

        if (!response.isSuccess()) return ResponseHandler.generateResponse(HttpStatus.OK, false, "success", "unverified");

        if (response.getData().getStatus().equals("PENDING")) {
            String status = response.getData().getStatus();
            int count = 0;
            while (status.equals("PENDING") && count < 10) {
                Thread.sleep(500);
                response = tranzakService.verifyCustomerPhoneNumberStatusBetter(dto.getCustomTransactionId());
                status = response.getData().getStatus();
                count = count + 1;
            }
            System.out.println(count);
            if(response.getData().getStatus().equals("COMPLETED")) {
                return ResponseHandler.generateResponse(HttpStatus.OK, false, "success", response.getData().getVerifiedName());
            } else {
                return ResponseHandler.generateResponse(HttpStatus.OK, false, "success", "unverified");
            }

        }
        return ResponseHandler.generateResponse(HttpStatus.OK, false, "success", response.getData().getVerifiedName());
    }

    @GetMapping(value = "/transactionStatus/{uuid}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> sendEventStream(@PathVariable String uuid, @AuthenticationPrincipal Subscriptions subscriber) throws Exception {

        Optional<MobilePayment> checkPay = mobilePaymentService.getPaymentTransactionStatus(uuid, subscriber.getClientMatricul())
                .stream()
                .findFirst();

        if (checkPay.isPresent()) {
            MobilePayment payment = checkPay.get();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("status", payment.getStatus().trim());
            jsonObject.put("message", payment.getStatus());
            return Flux.just(ServerSentEvent.<String>builder()
                    .id("1")
                    .event("status")
                    .data(jsonObject.toString())
                    .build());

        } else {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("status", "PAYMENT_NOT_FOUND");
            jsonObject.put("message", "The payment was not found.");

            return Flux.just(ServerSentEvent.<String>builder()
                    .id("1")
                    .event("status")
                    .data(jsonObject.toString())
                    .build());
        }
    }

    public static String generateUniqueReference() {
        // Obtenir la date et l'heure actuelles
        LocalDateTime now = LocalDateTime.now();

        // Formatter pour convertir la date et l'heure en chaîne
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

        // Convertir la date et l'heure en chaîne
        String dateTimeString = now.format(formatter);

        // Générer un UUID aléatoire
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");

        // Concaténer la date et l'heure avec l'UUID et prendre les 32 premiers caractères
        String reference = dateTimeString + uuid;

        // Assurer que la référence a une longueur de 32 caractères
        if (reference.length() > 32) {
            reference = reference.substring(0, 32);
        }

        return reference;
    }


}


