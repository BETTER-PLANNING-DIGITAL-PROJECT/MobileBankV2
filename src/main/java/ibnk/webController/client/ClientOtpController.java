package ibnk.webController.client;

import ibnk.dto.UserDto;
import ibnk.dto.auth.CustomerVerification;
import ibnk.dto.auth.OtpAuth;
import ibnk.models.internet.ClientVerification;
import ibnk.models.internet.client.Subscriptions;
import ibnk.models.internet.enums.OtpEnum;
import ibnk.repositories.internet.NotificationTemplateRepository;
import ibnk.service.CustomerService;
import ibnk.service.OtpService;
import ibnk.tools.ResponseHandler;
import ibnk.tools.error.UnauthorizedUserException;
import ibnk.tools.response.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/v1/client/otp")
@RequiredArgsConstructor
@CrossOrigin
public class ClientOtpController {
    private final OtpService otpService;
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final CustomerService customerService;
    private final TransactionController transactionController;
    private final AccountController accountController;


    @PostMapping("/verify/{guid}")
    public ResponseEntity<Object> VerifyOtpResetPassword(@AuthenticationPrincipal Subscriptions subscription, @PathVariable(value = "guid") String guid, @RequestBody OtpAuth otp, HttpServletRequest request) throws Exception {
        ClientVerification otpValidation = otpService.VerifyOtp(otp, guid, subscription,customerService.ip(request));
        if (otpValidation.isVerified()) {
            switch (OtpEnum.valueOf(otp.getRole())) {
                case VALIDATE_TRANSACTION -> {
                    return transactionController.validatedInitiatedOperation(subscription, guid);
                }case VALIDATE_BENEFICIARY -> {
                    return accountController.validatePendingBeneficiary(subscription, guid);
                }
                case VALIDATE_INTERNAL_TRANSACTION -> {
                    return transactionController.validateInitiatedTransfer(subscription,guid);
                }
//                case UPDATE_PIN -> {
//                    return customerService.ValidateUpdatePin(subscription);
//                }
//                case  RESET_PASSWORD -> {
//                    var result = customerService.verifyResetPassRequest(guid, otp);
//                    return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", result);
//                }
            }
            throw new UnauthorizedUserException("");
        }
        throw new UnauthorizedUserException("");
    }
//    @PostMapping("/verify/{guid}")
//    public ResponseEntity<Object> VerifyOtpResetPassword(@AuthenticationPrincipal Subscriptions subscription, @PathVariable(value = "guid") String guid, @RequestBody OtpAuth otp, HttpServletRequest request) throws Exception {
//        ClientVerification otpValidation = otpService.VerifyOtp(otp, guid, subscription,customerService.ip(request));
//        if (otpValidation.isVerified()) {
//            switch (OtpEnum.valueOf(otp.getRole())) {
//                case VALIDATE_TRANSACTION -> {
//                    return transactionController.validatedInitiatedOperation(subscription, guid);
//                }case VALIDATE_BENEFICIARY -> {
//                    return accountController.validatePendingBeneficiary(subscription, guid);
//                }
//                case VALIDATE_INTERNAL_TRANSACTION -> {
//                    return transactionController.validateInitiatedTransfer(subscription,guid);
//                } case UPDATE_PIN -> {
//                    return customerService.ValidateUpdatePin(subscription);
//                }
////                case  RESET_PASSWORD -> {
////                    var result = customerService.verifyResetPassRequest(guid, otp);
////                    return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", result);
////                }
//            }
//            throw new UnauthorizedUserException("");
//        }
//        throw new UnauthorizedUserException("");
//    }

    @GetMapping("resend-otp/{uuid}/{guid}")
    public ResponseEntity<Object> ResendOtp(@PathVariable(value = "uuid") String uuid, @AuthenticationPrincipal Subscriptions subscription, @PathVariable String guid) throws Exception {
        UserDto.CreateSubscriberClientDto clientDto = UserDto.CreateSubscriberClientDto.modelToDao(subscription);

        CustomerVerification result = otpService.GenerateResendOtp(uuid, subscription);

        AuthResponse<UserDto.CreateSubscriberClientDto, CustomerVerification> res = new AuthResponse<UserDto.CreateSubscriberClientDto, CustomerVerification>(clientDto, result);

        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", res);
    }

}

