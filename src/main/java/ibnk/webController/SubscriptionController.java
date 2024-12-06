package ibnk.webController;


import ibnk.dto.*;
import ibnk.dto.BankingDto.AccountEntityDto;
import ibnk.models.internet.UserEntity;
import ibnk.models.internet.client.ClientRequest;
import ibnk.service.BankingService.AccountService;
import ibnk.service.BankingService.MobilePaymentService;
import ibnk.service.ClientRequestService;
import ibnk.service.CustomerService;
import ibnk.tools.ResponseHandler;
import ibnk.tools.error.ResourceNotFoundException;
import ibnk.tools.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/api/v1/admin/")
public class SubscriptionController {
    private final CustomerService customerService;
    private final ClientRequestService clientRequestService;
    private final AccountService accountService;
    private final MobilePaymentService mobilePaymentService;


    @PostMapping("subscription/add")
    public ResponseEntity<Object> subscribe(@AuthenticationPrincipal UserEntity user, @RequestBody SubscriptionDao dao) throws SQLException, ResourceNotFoundException, ValidationException {
        UserDto.CreateSubscriberClientDto cus = customerService.AdminSubscribe(dao, user);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", cus);
    }

    @PostMapping("subscription/valid/{uuid}")
    public ResponseEntity<Object> getSubscriptionsValidationAdmin(@PathVariable(value = "uuid") String uuid, @AuthenticationPrincipal UserEntity user, @RequestBody SubscriptionDao dao) throws ResourceNotFoundException, ValidationException {
        List<AccountEntityDto> accountEntityDto = accountService.findClientAccounts(dao.getAccountId());
        AccountEntityDto accountInfo = accountEntityDto
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(dao.getAccountId() + " Does Not Exist Please Make Sure you have an Account"));

        customerService.handleSubscriptionValidation(dao, user.getUserLogin(), customerService.findClientByUuid(uuid));

        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", "Success");
    }

    @GetMapping("dashboard-status")
    public ResponseEntity<Object> dashBoard() {
        DashBoardTotalDto cus = clientRequestService.countStatusRequestAndSubStatus();
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", cus);
    }

    @GetMapping("subscription/listAll")
    public ResponseEntity<Object> ListAllSubscriptions(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "status", defaultValue = "") String status,
            @RequestParam(name = "type", defaultValue = "Physique") String type,
            @RequestParam(name = "filter") String property,
            @RequestParam(name = "search") String propertyValue,
            @RequestParam(name = "order", defaultValue = "ASC") String direction
    ) {
        var subs = customerService.AllSubscribers(page, size, direction, property, propertyValue, type, status);
        return ResponseHandler.generateResponse(HttpStatus.OK, false, "success", subs);
    }
    @GetMapping("transactions/list-all")
    public ResponseEntity<Object> ListAllTransactions(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "status", defaultValue = "") String status,
            @RequestParam(name = "filter") String property,
            @RequestParam(name = "search") String propertyValue,
            @RequestParam(name = "from") String from,
            @RequestParam(name = "to") String to,
            @RequestParam(name = "order", defaultValue = "ASC") String direction
    ) {
        var subs = accountService.allTransactions(page, size, direction, property, propertyValue, status, from,to);
        return ResponseHandler.generateResponse(HttpStatus.OK, false, "success", subs);
    }
    @PostMapping("transaction/reload")
    public ResponseEntity<Object> ReloadTransactions(
            @RequestParam(name = "pay-uuid") String telephone
            ) throws Exception {
       String response = mobilePaymentService.statusReload(telephone);
        return ResponseHandler.generateResponse(HttpStatus.OK, false, "success", response);
    }
    @GetMapping("subscription/list-client-request")
    public ResponseEntity<Object> pagingClientRequest(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "type", defaultValue = "Physique") String type,
            @RequestParam(name = "filter") String property,
            @RequestParam(name = "search") String propertyValue,
            @RequestParam(name = "status") String status,
            @RequestParam(name = "order", defaultValue = "ASC") String direction) {
        DataTable tableRequest = clientRequestService.listCustomersByType(page, size, direction, property, propertyValue, type, status);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", tableRequest);
    }

    @PutMapping("subscription/client-request-validation/{uuid}")
    public ResponseEntity<Object> validateClientAccountRequest(@PathVariable(name = "uuid") String uuid, @RequestBody ClientRequestDto dto, @AuthenticationPrincipal UserEntity user) {
        var subs = clientRequestService.checkCustomerInfo(uuid, dto, user);
        return ResponseHandler.generateResponse(HttpStatus.OK, false, "success", subs);
    }


    @GetMapping("subscription/client-request/{uuid}")
    public ResponseEntity<Object> ClientRequest(@PathVariable(name = "uuid") String uuid) throws ValidationException {
        Optional<ClientRequest> clientRequests = clientRequestService.findByUuid(uuid);
        ClientRequestDto.BasicRequestDto cltRqt = clientRequests.map(
                ClientRequestDto.BasicRequestDto::ModelToDto).orElseThrow(() -> new ValidationException("Detail_Not_Available")
        );
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", cltRqt);
    }
    @PutMapping("subscription/reset-security-questions/{clientid}")
    public ResponseEntity<Object> resetSecurityQuestions(@PathVariable String clientid) throws ResourceNotFoundException {
        String response = customerService.resetClientSecurityQuestions(clientid);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", response);
    }



}


