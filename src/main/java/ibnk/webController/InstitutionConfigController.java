package ibnk.webController;

import ibnk.dto.BankingDto.MobileBankConfigDto;
import ibnk.dto.DeviceDto;
import ibnk.dto.UserDto;
import ibnk.dto.clientSecurityUpdateDto;
import ibnk.models.internet.*;
import ibnk.models.internet.client.SecurityQuestions;
import ibnk.repositories.internet.ClientSecurityQuestionRepository;
import ibnk.service.BankingService.AccountService;
import ibnk.service.CustomerService;
import ibnk.service.InstitutionConfigService;
import ibnk.service.QuestionService;
import ibnk.service.UserService;
import ibnk.tools.ResponseHandler;
import ibnk.tools.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

/**
 * @author PHILF
 */
@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/api/v1/admin/instConfig")
public class InstitutionConfigController {

    private final InstitutionConfigService institutionConfigService;
    private final AccountService accountService;
    private final CustomerService customerService;
    private final UserService userService;
    private final QuestionService questionService;
    private final ClientSecurityQuestionRepository clientSecurityQuestionRepository;


    @GetMapping("list-email-server")
    public ResponseEntity<Object> listEmailServer() {
        List<EmailServer> emailServerList = institutionConfigService.listEmailServer();
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", emailServerList);
    }

    @PutMapping("update-email-server/{id}")
    public ResponseEntity<Object> updateEmailServer(@PathVariable Long id, @RequestBody EmailServer emailServer) throws ResourceNotFoundException {
        String emailServerList = institutionConfigService.updateServer(id, emailServer);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", emailServerList);
    }

    @DeleteMapping("delete-email-server/{id}")
    public ResponseEntity<Object> deleteEmailServer(@PathVariable Long id) {
        String emailServerList = institutionConfigService.DeleteEmailServer(id);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", emailServerList);
    }

    @PostMapping("save-email-server/{id}")
    public ResponseEntity<Object> saveEmailServer(@RequestBody EmailServer emailServer) throws ResourceNotFoundException {
        String emailServerList = institutionConfigService.saveServer(emailServer);

        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", emailServerList);
    }

    @PutMapping("update")
    public ResponseEntity<Object> updateInstConfig(@RequestBody InstitutionConfig dto) throws  ResourceNotFoundException {
        String instConfig = institutionConfigService.updateInstitutionConfig(dto);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", instConfig);
    }

    @GetMapping("listInstConfig")
    public ResponseEntity<Object> listInstConfig()  {
        InstitutionConfig institutionConfig = institutionConfigService.getInstConfig();
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", institutionConfig);

    }

    @GetMapping("listMobileConfig")
    public ResponseEntity<Object> listMobileBankConfig() throws SQLException {
        List<MobileBankConfigDto> mobileBankConfigDto = accountService.findMbConfig();
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", mobileBankConfigDto);

    }

    @GetMapping("listQuestions")
    public ResponseEntity<Object> listQuestion() {
        List<SecurityQuestions> securityQuestions = questionService.listAllQuestions();
        securityQuestions.forEach((q -> q.setQuestionCount(clientSecurityQuestionRepository.countBySecurityQuestions(q))));
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", securityQuestions);
    }

    @PostMapping("addQuestion")
    public ResponseEntity<Object> addQuestions(@RequestBody SecurityQuestions dao)  {
        String securityQuestions = questionService.saveSecurityQuestions(dao);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", securityQuestions);
    }

    @PutMapping("update-transaction-message")
    public ResponseEntity<Object> updateTransactionMessages(@RequestBody TransactionStatusMessage dto) throws ResourceNotFoundException {
        String response = institutionConfigService.UpdateTransactionMessageById(dto.getId(), dto);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", response);
    }

    @GetMapping("list-transaction-message")
    public ResponseEntity<Object> listTransactionMessages() {
        List<TransactionStatusMessage> response = institutionConfigService.listTransactionMessage();
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", response);
    }

    @DeleteMapping("deleteQuestion/{id}")
    public ResponseEntity<Object> deleteQuestions(@PathVariable Long id) {
        String securityQuestions = questionService.deleteSecurityQuestion(id);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", securityQuestions);
    }

    @PutMapping("update-notification-template")
    public ResponseEntity<Object> updateNotification(@RequestBody NotificationTemplate dto) throws ResourceNotFoundException {
        String response = institutionConfigService.UpdateNotifTemplate(dto.getId(), dto);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", response);
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<Object> deleteNotification(@PathVariable("id") Long id) throws ResourceNotFoundException {
        String response = institutionConfigService.deleteNotifById(id);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", response);

    }

    @GetMapping("list-templates")
    public ResponseEntity<Object> listNotification() {
        List<NotificationTemplate> response = institutionConfigService.listNotifTemplate();
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", response);
    }

    @GetMapping("list-term")
    public ResponseEntity<Object> listTerm() {
        List<TermAndCondition> response = institutionConfigService.ListTerm();
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", response);
    }

    @GetMapping("find-term{code}")
    public ResponseEntity<Object> findTermByCode(@PathVariable(value = "code") String code) throws ResourceNotFoundException {
        TermAndCondition response = institutionConfigService.findTermAndConditionByCode(code);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", response);
    }

    @PostMapping("save-term")
    public ResponseEntity<Object> saveTerm(@RequestBody TermAndCondition dto) throws ResourceNotFoundException {
        String response = institutionConfigService.SaveTerm(dto);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", response);
    }

    @PutMapping("update-term")
    public ResponseEntity<Object> updateTerm(@RequestBody TermAndCondition dto) throws ResourceNotFoundException {
        String response = institutionConfigService.UpdateTerm(dto);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", response);
    }

    @DeleteMapping("delete-term/{id}")
    public ResponseEntity<Object> deleteTerm(@PathVariable(value = "id") long id) {
        String response = institutionConfigService.DeleteTerm(id);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", response);
    }

    @GetMapping("client-details/{clientid}")
    public ResponseEntity<Object> clientDetail(@PathVariable("clientid") String clientid) {
        UserDto.CreateSubscriberClientDto response = customerService.findSubscriberByClientId(clientid);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", response);
    }

    @PutMapping("update-client-status/{clientid}")
    public ResponseEntity<Object> updatClientStatus(@RequestBody clientSecurityUpdateDto body, @PathVariable("clientid") String clientid) {
        String response = customerService.updateClientStatus(body, clientid);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", response);
    }
    @PostMapping("reset-client-device/{uuid}")
    public ResponseEntity<Object> ResetAllClientDevices(@PathVariable(name = "uuid") String uuid )  {
        userService.archiveClientDevices(uuid);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", "success");
    }
//    @PostMapping("reset-client-device/{uuid}/{deviceUuid}")
//    public ResponseEntity<Object> ResetClientDevice(@PathVariable(name = "uuid") String uuid,@PathVariable(name = "deviceUuid") String deviceId ) throws  ResourceNotFoundException {
//
//        userService.archiveClientDevice(deviceId,customerService.findClientByUuid(uuid));
//        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", "success");
//    }
    @GetMapping("list-client-device/{uuid}")
    public ResponseEntity<Object> ClientDeviceList(@PathVariable(name = "uuid") String uuid ) {
        List<DeviceDto>  tableRequest = userService.listDevices(uuid);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", tableRequest);
    }
}
