package ibnk.tools.Interceptors;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ibnk.dto.BankingDto.ClientQuestDto;
import ibnk.dto.auth.CustomerVerification;
import ibnk.models.internet.ClientVerification;
import ibnk.models.internet.InstitutionConfig;
import ibnk.models.internet.client.Subscriptions;
import ibnk.models.internet.enums.Application;
import ibnk.models.internet.enums.Status;
import ibnk.models.internet.enums.VerificationType;
import ibnk.repositories.internet.ClientVerificationRepository;
import ibnk.service.InstitutionConfigService;
import ibnk.tools.error.FailedSecurityVerification;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import reactor.util.annotation.NonNull;


@Component
public class SecurityQuestionInterceptor implements HandlerInterceptor {


    private final ClientVerificationRepository clientVerificationRepository;
    private final InstitutionConfigService institutionConfigService;

    @Autowired
    public SecurityQuestionInterceptor(ClientVerificationRepository clientVerificationRepository, InstitutionConfigService institutionConfigService) {
        this.clientVerificationRepository = clientVerificationRepository;
        this.institutionConfigService = institutionConfigService;
    }

    @Override
    public boolean preHandle(@NonNull  HttpServletRequest request,@NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        if (handler instanceof HandlerMethod handlerMethod) {
            Method method = handlerMethod.getMethod();
            if (method.isAnnotationPresent(InterceptQuestions.class)) {
                Subscriptions subscription = (Subscriptions) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                try {
                    String securityQuestionsJson = request.getHeader("X-User-Security");
                    if (securityQuestionsJson == null) {
                        throw new FailedSecurityVerification("Security questions are incorrect", null);
                    }

                    List<ClientQuestDto> securityQuestionsDTO = transformJsonToDTO(securityQuestionsJson);

                    if (securityQuestionsDTO == null) {
                        throw new FailedSecurityVerification("Security questions list are incorrect", null);
                    }
                    try {
                        institutionConfigService.verifySecurityQuestions(securityQuestionsDTO, subscription);
                    } catch (Exception e) {
                        throw new FailedSecurityVerification(e.getMessage(), null);
                    }
                } catch (Exception ex) {
                    ClientVerification verify = ClientVerification.builder()
                            .subscriptions(subscription)
                            .status(Status.FAILED)
                            .verified(false)
                            .verificationType(VerificationType.SECURITY_QUESTION)
                            .message(ex.getMessage())
                            .build();
                    clientVerificationRepository.save(verify);

                    InstitutionConfig config = institutionConfigService.findByyApp(Application.MB.name());
                    CustomerVerification verificationObject = institutionConfigService.countRemainingCustomerTrials(subscription,config,  VerificationType.SECURITY_QUESTION);
                    throw new FailedSecurityVerification(ex.getMessage(),verificationObject);
                }
            }
            return true;
        }
        return true;
    }


    private List<ClientQuestDto> transformJsonToDTO(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, new TypeReference<List<ClientQuestDto>>() {});
    }


}
