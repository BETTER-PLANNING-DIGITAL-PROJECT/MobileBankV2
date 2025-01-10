package ibnk.webController.client;

import com.crystaldecisions.sdk.occa.report.lib.ReportSDKExceptionBase;
import ibnk.dto.AccountHistoryDto;
import ibnk.dto.BankingDto.*;
import ibnk.dto.NotificationEvent;
import ibnk.dto.UserDto;
import ibnk.dto.auth.CustomerVerification;
import ibnk.models.banking.MobileBeneficiairy;
import ibnk.models.banking.MobileBeneficiairyEntity;
import ibnk.models.internet.OtpEntity;
import ibnk.models.internet.client.Subscriptions;
import ibnk.models.internet.enums.*;
import ibnk.repositories.banking.MobileBeneficiaryRepository;
import ibnk.service.BankingService.AccountService;
import ibnk.service.InstitutionConfigService;
import ibnk.service.OtpService;
import ibnk.service.ReportServiceRpt;
import ibnk.tools.Interceptors.InterceptPin;
import ibnk.tools.Interceptors.InterceptQuestions;
import ibnk.tools.ResponseHandler;
import ibnk.tools.TOOLS;
import ibnk.tools.error.ResourceNotFoundException;
import ibnk.tools.error.UnauthorizedUserException;
import ibnk.tools.error.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/api/v1/client/accounts")
public class AccountController {
    private final AccountService accountService;
    private final OtpService otpService;
    private final ReportServiceRpt reportServiceRpt;
    private final MobileBeneficiaryRepository mobileBeneficiaryRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final InstitutionConfigService institutionConfigService;

    @CrossOrigin
    @GetMapping("listAll")
    public ResponseEntity<Object> findClientAccount(
            @AuthenticationPrincipal Subscriptions subscriber,
            @RequestParam(name = "operation", required = false, defaultValue = "default") String operation ) {
        List<AccountEntityDto> cus = accountService.findClientAccountsByOperation(subscriber.getClientMatricul(), operation);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", cus);
    }

    @GetMapping("get-balance/{accountId}")
    @InterceptPin()
    public ResponseEntity<Object> getAccountBalance(@PathVariable String accountId, @AuthenticationPrincipal Subscriptions subscriber) throws ValidationException {
        AccountBalanceDto cus = accountService.findAccountBalancesWithAccount(accountId, subscriber.getClientMatricul());
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", cus);
    }


    @PostMapping("service-charge")
    public ResponseEntity<Object> ServiceCharge(@RequestBody() AccountMvtDto json, @AuthenticationPrincipal Subscriptions subscriptions, HttpServletRequest request) throws ValidationException, BadRequestException {

      accountService.ServiceCharge(json,subscriptions,request);
        return ResponseHandler.generateResponse(HttpStatus.OK, false, "success", "Success");
    }



    @GetMapping("getDetails")
    public ResponseEntity<Object> getAllAccountsDetails(@AuthenticationPrincipal Subscriptions subscriber) {
        List<AccountEntityDto> cus = accountService.findClientAccounts(subscriber.getClientMatricul());
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", cus);
    }

    @PostMapping("statement-report")
    public ResponseEntity<Object> Reportgenerate(@RequestBody AccountHistoryDto dao, @AuthenticationPrincipal Subscriptions sub, @RequestParam String mimiType) throws ReportSDKExceptionBase, IOException, ResourceNotFoundException {

        switch (RangeSelector.valueOf(dao.getRangeSelector())) {
            case THIS_MONTH -> {
                dao.setOpeningDate(TOOLS.firstDayOfThisMonth());
                dao.setClosingDate(LocalDate.now());
            }
            case PREVIOUS_MONTH -> {
                dao.setOpeningDate(TOOLS.firstDayOfPreviousMonth());
                dao.setClosingDate(TOOLS.lastDayOfPreviousMonth());
            }
            case PREVIOUS_QUARTER -> {
                dao.setOpeningDate(TOOLS.firstDayOfPreviousQuarterMonth());
                dao.setClosingDate(TOOLS.lastDayOfPreviousQuarterMonth());
            }
        }
        byte[] pdfBytes = null;
        switch (mimiType) {
            case "application/pdf":
                pdfBytes = reportServiceRpt.clientReportStatment(dao, sub, "application/pdf");
                break;
            // Add cases for other supported MIME types if needed
            case "application/excel":
                pdfBytes = reportServiceRpt.clientReportStatment(dao, sub, mimiType);
                break;
            default:
                // Handle unsupported MIME types
                return ResponseEntity.badRequest().body("Unsupported mimeType: " + mimiType);
        }
        return TOOLS.getObjectResponseEntity(mimiType, pdfBytes);
    }

    @PostMapping("e-statement-report")
    public ResponseEntity<Object> EReportGenerate(@RequestBody AccountHistoryDto dao, @AuthenticationPrincipal Subscriptions sub, @RequestParam String mimiType) throws ReportSDKExceptionBase, IOException, ResourceNotFoundException {

        switch (RangeSelector.valueOf(dao.getRangeSelector())) {
            case THIS_MONTH -> {
                dao.setOpeningDate(TOOLS.firstDayOfThisMonth());
                dao.setClosingDate(LocalDate.now());
            }
            case PREVIOUS_MONTH -> {
                dao.setOpeningDate(TOOLS.firstDayOfPreviousMonth());
                dao.setClosingDate(TOOLS.lastDayOfPreviousMonth());
            }
            case PREVIOUS_QUARTER -> {
                dao.setOpeningDate(TOOLS.firstDayOfPreviousQuarterMonth());
                dao.setClosingDate(TOOLS.lastDayOfPreviousQuarterMonth());
            }
        }
        String pdfBytes = null;
        switch (mimiType) {
            case "application/pdf":
                pdfBytes = reportServiceRpt.eClientReportStatement(dao, sub, "application/pdf");
                break;
            // Add cases for other supported MIME types if needed
            case "application/excel":
                pdfBytes = reportServiceRpt.eClientReportStatement(dao, sub, mimiType);
                break;
            default:
                // Handle unsupported MIME types
                return ResponseEntity.badRequest().body("Unsupported mimeType: " + mimiType);
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, false, "success", pdfBytes);
    }

    @PostMapping("account-report")
    public ResponseEntity<Object> ReportgenerateAccountInfo(@RequestBody AccountHistoryDto dao, @AuthenticationPrincipal Subscriptions sub, @RequestParam String mimiType) throws ReportSDKExceptionBase, IOException, ResourceNotFoundException {

        //        switch (RangeSelector.valueOf(dao.getRangeSelector())) {
        //            case THIS_MONTH -> {
        //                dao.setOpeningDate(TOOLS.firstDayOfThisMonth());
        //                dao.setClosingDate(LocalDate.now());
        //            }
        //            case PREVIOUS_MONTH -> {
        //                dao.setOpeningDate(TOOLS.firstDayOfPreviousMonth());
        //                dao.setClosingDate(TOOLS.lastDayOfPreviousMonth());
        //            }
        //            case PREVIOUS_QUARTER -> {
        //                dao.setOpeningDate(TOOLS.firstDayOfPreviousQuarterMonth());
        //                dao.setClosingDate(TOOLS.lastDayOfPreviousQuarterMonth());
        //            }
        //        }
        byte[] pdfBytes = null;
        switch (mimiType) {
            case "application/pdf":
                pdfBytes = reportServiceRpt.rptAcctOpenInfo(dao.getAccountId(), "application/pdf");
                break;
            // Add cases for other supported MIME types if needed
            case "application/excel":
                pdfBytes = reportServiceRpt.rptAcctOpenInfo(dao.getAccountId(), mimiType);
                break;
            default:
                // Handle unsupported MIME types
                return ResponseEntity.badRequest().body("Unsupported mimeType: " + mimiType);
        }
        return TOOLS.getObjectResponseEntity(mimiType, pdfBytes);
    }


    @PostMapping("get-statement")
    @InterceptPin()
    public ResponseEntity<Object> ListAllAccountHistory(@RequestBody AccountHistoryDto dao, @AuthenticationPrincipal Subscriptions subscriber) throws ResourceNotFoundException {
        //        DateTimeFormatter customPattern = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        switch (RangeSelector.valueOf(dao.getRangeSelector())) {
            case THIS_MONTH -> {
                dao.setOpeningDate(TOOLS.firstDayOfThisMonth());
                dao.setClosingDate(LocalDate.now());
            }
            case PREVIOUS_MONTH -> {
                dao.setOpeningDate(TOOLS.firstDayOfPreviousMonth());
                dao.setClosingDate(TOOLS.lastDayOfPreviousMonth());
            }
            case PREVIOUS_QUARTER -> {
                dao.setOpeningDate(TOOLS.firstDayOfPreviousQuarterMonth());
                dao.setClosingDate(TOOLS.lastDayOfPreviousQuarterMonth());
            }
        }
        AccountHistoryRes accountHistoryRes = accountService.clientAccountHistory(dao, subscriber);
        return ResponseHandler.generateResponse(HttpStatus.OK, false, "success", accountHistoryRes.getHistoryDto());
    }

    @PostMapping("recent-activity")
    @InterceptPin()
    public ResponseEntity<Object> ListRecentActivity(@RequestBody AccountHistoryDto dao, @AuthenticationPrincipal Subscriptions subscriber) throws SQLException, ResourceNotFoundException {
        var hist = accountService.getClientActivity(dao, subscriber.getClientMatricul());
        return ResponseHandler.generateResponse(HttpStatus.OK, false, "success", hist);
    }

    @GetMapping("find-account/{accountId}")
    public ResponseEntity<Object> findAccountByAccountNumber(@PathVariable String accountId) throws ResourceNotFoundException {
        List<AccountEntityDto> cus = accountService.findClientAccounts(accountId);
        if (cus.isEmpty()) throw new ResourceNotFoundException("account_not_found");
        if (cus.size() > 1) throw new ResourceNotFoundException("account_not_found");
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", cus.get(0));
    }

    @InterceptPin
    @PostMapping("save-beneficiary")
    public ResponseEntity<Object> getBeneficiary(@RequestBody BeneficiaryDto beneficiaryDto, @AuthenticationPrincipal Subscriptions subscriber) throws ValidationException, UnauthorizedUserException, ResourceNotFoundException {

       if(Objects.equals(beneficiaryDto.getPhoneNumber(), subscriber.getPhoneNumber())  ) {
           throw new ValidationException("beneficiary-exist");
       }
        BeneficiaryDto data = accountService.saveBeneficiary(beneficiaryDto, subscriber);

        OtpEntity params = OtpEntity.builder()
                .guid(data.getUuid())
                .email(subscriber.getEmail())
                .phoneNumber(subscriber.getPhoneNumber())
                .role(OtpEnum.VALIDATE_BENEFICIARY)
                .transport(subscriber.getPreferedNotificationChanel())
                .build();

        List<Object> payloads = new ArrayList<>();
        payloads.add(data);
        payloads.add(UserDto.CreateSubscriberClientDto.modelToDao(subscriber));
        CustomerVerification verificationObject = otpService.GenerateAndSend(params, payloads, subscriber);

        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", verificationObject);
    }

    public ResponseEntity<Object> validatePendingBeneficiary(Subscriptions subscriber, String uuid) throws Exception {
        MobileBeneficiairyEntity beneficiary;
        List<Object> payload = new ArrayList<>();

        try {
            // Retrieve beneficiary and update its status based on configuration
            beneficiary = accountService.findBeneficiaryByUuid(uuid);
            String beneficiaryStatus = institutionConfigService.findByyApp(Application.MB.name()).getBenefApprov().equals("NO")
                    ? Status.APPROVED.name()
                    : Status.NOTAPPROVED.name();
            beneficiary.setStatus(beneficiaryStatus);
            mobileBeneficiaryRepository.save(beneficiary);

            // Prepare payload
            payload.add(beneficiary);
            payload.add(UserDto.CreateSubscriberClientDto.modelToDao(subscriber));

            // Determine the event code
            EventCode eventCode = beneficiaryStatus.equals(Status.APPROVED.name())
                    ? EventCode.BENEFICIARY_APPROVED
                    : EventCode.BENEFICIARY_NOTAPPROVED;

            // Create and publish the notification event
            publishNotificationEvent(subscriber, payload, eventCode);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ValidationException("Something went wrong, please try again later.");
        }

        return ResponseHandler.generateResponse(HttpStatus.OK, false, "Success", beneficiary);
    }

    /**
     * Publishes a notification event with the provided details.
     */
    private void publishNotificationEvent(Subscriptions subscriber, List<Object> payload, EventCode eventCode) {
        NotificationEvent event = new NotificationEvent();
        event.setEventCode(eventCode.name());
        event.setPayload(payload);
        event.setType(subscriber.getPreferedNotificationChanel());
        event.setPhoneNumber(subscriber.getPhoneNumber());
        event.setEmail(subscriber.getEmail());
        event.setSubscriber(subscriber);
        applicationEventPublisher.publishEvent(event);
    }
    @GetMapping("list-beneficiaries")
    public ResponseEntity<Object> getAllBeneficiaryDetails(@AuthenticationPrincipal Subscriptions subscriber) throws SQLException {
        List<BeneficiaryDto> beneficiaries = accountService.findBeneficiaryByClientId(subscriber.getClientMatricul());
        BeneficiaryDto benef = new BeneficiaryDto();
        benef.setName(subscriber.getClientName() + " ( You )");
        benef.setPhoneNumber(subscriber.getPhoneNumber());
        benef.setBeneficiaire("mobile");
        benef.setAgence(subscriber.getBranchCode());
        benef.setBenefactorAccountNumber("mobile");
        beneficiaries.add(0, benef);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", beneficiaries);
    }

    @InterceptPin
    @DeleteMapping("delete-beneficiary/{id}")
    public ResponseEntity<Object> deleteBeneficiary(@PathVariable Integer id, @AuthenticationPrincipal Subscriptions subscriber) throws SQLException, ValidationException {
        BeneficiaryDto cus = accountService.deleteBeneficiary(id, subscriber);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", cus);
    }

    @InterceptQuestions
    @PostMapping("check-book-request")
    public ResponseEntity<Object> checkBookRequest(@RequestBody CheckbookRequestDto checkbookRequest, @AuthenticationPrincipal Subscriptions subscriber) throws ValidationException {
        String response = accountService.checkbookRequest(checkbookRequest, subscriber);

        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", response);
    }

    @GetMapping("list-checkbook-request")
    public ResponseEntity<Object> checkBookRequest(@AuthenticationPrincipal Subscriptions subscriber) throws SQLException {
        List<CheckbookRequestDto> checkbookRequestDto = accountService.listChequeBook(subscriber);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", checkbookRequestDto);
    }

    @DeleteMapping("delete-checkbook-request/{id}")
    public ResponseEntity<Object> deleteCheckBookRequest(@PathVariable Long id, @AuthenticationPrincipal Subscriptions subscriber) throws ValidationException {
        String checkbookRequestDto = accountService.deleteCheckbookRequest(id, subscriber);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", checkbookRequestDto);
    }

    @InterceptQuestions
    @PostMapping("stop-check-payment")
    public ResponseEntity<Object> stopPaymentChecks(@RequestBody CheckbookRequestDto.ChequeSeries series, @AuthenticationPrincipal Subscriptions subscriber) throws ResourceNotFoundException, SQLException {
        String checkbookRequestDto = accountService.stopPaymentInsert(series, subscriber.getClientMatricul());
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", checkbookRequestDto);
    }

    @GetMapping("list-stop-checks/{accountId}")
    public ResponseEntity<Object> listStopPayment(@PathVariable String accountId, @AuthenticationPrincipal Subscriptions subscriber) throws SQLException {
        List<CheckbookRequestDto.StopPay> stopPays = accountService.listCheckStoppedByAccountId(accountId, subscriber.getClientMatricul());
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", stopPays);
    }

    @InterceptQuestions
    @PostMapping("open-check-payment")
    public ResponseEntity<Object> deleteStopPayment(@RequestBody CheckbookRequestDto.StopPay stop, @AuthenticationPrincipal Subscriptions subscriber) throws ValidationException {
        String checkbookRequestDto = accountService.OppliftStopPayment(stop, subscriber.getClientMatricul());
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", checkbookRequestDto);
    }

    @GetMapping("check-series/{accountId}")
    public ResponseEntity<Object> chequeSeries(@PathVariable String accountId, @AuthenticationPrincipal Subscriptions subscriber) throws SQLException {
        List<CheckbookRequestDto.ChequeSeries> chequeSeries = accountService.chequeSeriesByAccountId(accountId, subscriber.getClientMatricul());
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", chequeSeries);
    }

}
