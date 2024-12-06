package ibnk.tools.Interceptors;

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
import ibnk.repositories.internet.SubscriptionRepository;
import ibnk.service.InstitutionConfigService;
import ibnk.tools.error.FailedSecurityVerification;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import reactor.util.annotation.NonNull;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
@Service
public class PinInterceptor implements HandlerInterceptor {


    private final ClientVerificationRepository clientVerificationRepository;
    private final InstitutionConfigService institutionConfigService;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    public PinInterceptor(InstitutionConfigService institutionConfigService,ClientVerificationRepository clientVerificationRepository,PasswordEncoder passwordEncoder) {
        this.clientVerificationRepository = clientVerificationRepository;
        this.institutionConfigService = institutionConfigService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        if (handler instanceof HandlerMethod handlerMethod) {
            Method method = handlerMethod.getMethod();
            if (method.isAnnotationPresent(InterceptPin.class)) {
                Subscriptions subscription = (Subscriptions) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                try {
                    String rawPin = request.getHeader("T");
                    if (rawPin == null) {
                        throw new FailedSecurityVerification("Wrong pin", null);
                    }
                       String  encodedPin = subscription.getPins();
                        if(!passwordEncoder.matches(rawPin,encodedPin)){
                            throw new FailedSecurityVerification("Wrong pin",null);
                        }

                    institutionConfigService.archiveClientVerifications(subscription, VerificationType.PIN);
                } catch (Exception ex) {
                    ClientVerification verify = ClientVerification.builder()
                            .subscriptions(subscription)
                            .status(Status.FAILED)
                            .verified(false)
                            .verificationType(VerificationType.PIN)
                            .message(ex.getMessage())
                            .build();
                    clientVerificationRepository.save(verify);

                    InstitutionConfig config = institutionConfigService.findByyApp(Application.MB.name());
                    CustomerVerification verificationObject = institutionConfigService.countRemainingCustomerTrials(subscription,config,  VerificationType.PIN);
                    throw new FailedSecurityVerification(ex.getMessage() + " " + (verificationObject.getTrials() - 1L) + " trials left",verificationObject);
                }
            }
            return true;
        }
        return true;
    }



}
