package ibnk.webController.client;

import ibnk.dto.BankingDto.ClientQuestDto;
import ibnk.dto.UserDto;
import ibnk.dto.auth.CustomerVerification;
import ibnk.models.internet.ClientVerification;
import ibnk.models.internet.InstitutionConfig;
import ibnk.models.internet.client.ClientConfig;
import ibnk.models.internet.client.SecurityQuestions;
import ibnk.models.internet.client.Subscriptions;
import ibnk.models.internet.enums.Status;
import ibnk.models.internet.enums.VerificationType;
import ibnk.repositories.internet.ClientSecurityQuestionRepository;
import ibnk.repositories.internet.ClientVerificationRepository;
import ibnk.repositories.internet.SecurityQuestionRepository;
import ibnk.service.ClientSecurityQuestService;
import ibnk.service.CustomerService;
import ibnk.service.InstitutionConfigService;
import ibnk.tools.Interceptors.InterceptQuestions;
import ibnk.tools.ResponseHandler;
import ibnk.tools.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/api/v1/client/config")
public class ConfigClientController {

    private final ClientSecurityQuestService clientSecurityQuestService;
    private final CustomerService customerService;
    private final ClientSecurityQuestionRepository clientSecurityQuestionRepository;
    private final SecurityQuestionRepository securityQuestionRepository;
    private final InstitutionConfigService institutionConfigService;
    private final ClientVerificationRepository clientVerificationRepository;

    @PostMapping("save-client-config")
    public ResponseEntity<Object> saveClientConfig(@RequestBody ClientConfig dto, @AuthenticationPrincipal Subscriptions subs) throws SQLException, ResourceNotFoundException {
        dto.setSubscriptions(subs);
        String clientConfig = institutionConfigService.saveClientConfig(dto, subs);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", clientConfig);
    }
    @PatchMapping("update-client-config")
    public ResponseEntity<Object> updateClientConfig(@RequestBody ClientConfig dto,@AuthenticationPrincipal Subscriptions subs) throws ResourceNotFoundException {
        dto.setSubscriptions(subs);
        String clientConfig = institutionConfigService.updateClientConfig(dto, subs);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", clientConfig);
    }

    @GetMapping("list-client-config")
    public ResponseEntity<Object> listClientConfig(@AuthenticationPrincipal Subscriptions sub) throws  SQLException {
        List<ClientConfig> institutionConfig = institutionConfigService.listClientConfig(sub);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", institutionConfig);
    }

    @GetMapping("client-config/{id}")
    public ResponseEntity<Object> getClientConfigById(@AuthenticationPrincipal Subscriptions sub, @PathVariable Long id) throws  SQLException {
        ClientConfig clientConfig = institutionConfigService.getClientConfigById(id,sub);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", clientConfig);
    }
    @GetMapping
    public ResponseEntity<Object> listInstConfig() throws ResourceNotFoundException {
        InstitutionConfig institutionConfig = institutionConfigService.getInstConfig();
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", institutionConfig);

    }
    @PostMapping("/setSecurityQuestions")
    public ResponseEntity<Object> setSecurityQuestions(@RequestBody List<ClientQuestDto> dto, @AuthenticationPrincipal Subscriptions subs) throws ResourceNotFoundException {
        String response = clientSecurityQuestService.saveClientQuestion(dto,subs);
        return ResponseHandler.generateResponse(HttpStatus.OK, false, "success", response);
    }

    @InterceptQuestions
    @PostMapping("/updateSecurityQuestions")
    public ResponseEntity<Object> updateSecurityQuestions(@RequestBody List<ClientQuestDto> dto, @AuthenticationPrincipal Subscriptions subs) throws ResourceNotFoundException {
        String response = clientSecurityQuestService.saveClientQuestion(dto,subs);
        return ResponseHandler.generateResponse(HttpStatus.OK, false, "success", response);
    }

    @GetMapping("/listSecurityQuestions")
    public ResponseEntity<Object> listSecurityQuestions( @AuthenticationPrincipal Subscriptions subs) throws ResourceNotFoundException {
        List<SecurityQuestions> response = securityQuestionRepository.findAll();
        return ResponseHandler.generateResponse(HttpStatus.OK, false, "success", response);
    }

    @GetMapping("/getVerificationQuestions")
    public ResponseEntity<Object> listVerificationQuestion( @AuthenticationPrincipal Subscriptions subs) throws ResourceNotFoundException {
        InstitutionConfig config = institutionConfigService.getInstConfig();
        List<SecurityQuestions> response = clientSecurityQuestionRepository.listClientSecurityQuestionsRandomly(subs.getId()).subList(0, config.getVerifyQuestNumber());

        LocalDateTime time =  LocalDateTime.now().minusMinutes(config.getVerificationResetTimer());
        Integer previousTrials = clientVerificationRepository.countPreviousFailedTrials(subs, Status.FAILED, time, VerificationType.SECURITY_QUESTION);
        Integer leftTrials = Math.toIntExact(config.getMaxVerifyAttempt() - previousTrials);
        CustomerVerification responseData = new CustomerVerification();

        responseData.setVerificationObject(response);
        responseData.setTrials(leftTrials);
        responseData.setMaxTrials(Math.toIntExact(config.getMaxVerifyAttempt()));
        responseData.setVerificationType("SECURE_QUESTIONS");
        return ResponseHandler.generateResponse(HttpStatus.OK, false, "success", responseData);
    }

    @PostMapping("/verifySecurityQuestions")
    public ResponseEntity<Object> verifySecurityQuestions(@RequestBody List<ClientQuestDto> dto, @AuthenticationPrincipal Subscriptions subs) throws ResourceNotFoundException {
        ClientVerification response = institutionConfigService.verifySecurityQuestions(dto,subs);
        return ResponseHandler.generateResponse(HttpStatus.OK, false, "success", response);
    }
    @GetMapping("client-details/{clientid}")
    public ResponseEntity<Object> clientDetail(@PathVariable("clientid") String clientid) {
        UserDto.CreateSubscriberClientDto response = customerService.findSubscriberByClientId(clientid);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", response);
    }


}
