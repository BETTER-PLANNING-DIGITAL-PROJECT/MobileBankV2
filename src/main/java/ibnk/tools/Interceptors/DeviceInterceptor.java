package ibnk.tools.Interceptors;

import com.fasterxml.jackson.databind.ObjectMapper;
import ibnk.dto.auth.CustomerVerification;
import ibnk.models.internet.ClientVerification;
import ibnk.models.internet.InstitutionConfig;
import ibnk.models.internet.client.ClientDevice;
import ibnk.models.internet.client.Subscriptions;
import ibnk.models.internet.enums.Application;
import ibnk.models.internet.enums.Status;
import ibnk.models.internet.enums.VerificationType;
import ibnk.repositories.internet.ClientDeviceRepository;
import ibnk.tools.error.FailedSecurityVerification;
import ibnk.tools.error.UnauthorizedUserException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import reactor.util.annotation.NonNull;

import java.lang.reflect.Method;

@Service
public class DeviceInterceptor implements HandlerInterceptor {

    private ClientDeviceRepository clientDeviceRepository;
    public DeviceInterceptor(ClientDeviceRepository clientDeviceRepository){
        this.clientDeviceRepository = clientDeviceRepository;
    }
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        if (handler instanceof HandlerMethod handlerMethod) {
            Method method = handlerMethod.getMethod();
            if (method.isAnnotationPresent(DeviceExtractor.class)) {
                Subscriptions subscription = (Subscriptions) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                try {
                    String device = request.getHeader("X-Device");
//                    if (device == null) {
//                        throw new UnauthorizedUserException("Required_Device");
//                    }
                    ObjectMapper objectMapper = new ObjectMapper();
                    ClientDevice deviceInfo = objectMapper.readValue(device, ClientDevice.class);

                    clientDeviceRepository.save(deviceInfo);
//                    String  encodedPin = subscription.getPins();
//                    if(!passwordEncoder.matches(rawPin,encodedPin)){
//                        throw new FailedSecurityVerification("pin incorrect",null);
//                    }
                }catch (Exception ex){
                    throw new UnauthorizedUserException(ex.getMessage());
                }
//                catch (Exception ex) {
//                    ClientVerification verify = ClientVerification.builder()
//                            .subscriptions(subscription)
//                            .status(Status.FAILED)
//                            .verified(false)
//                            .verificationType(VerificationType.PIN)
//                            .message(ex.getMessage())
//                            .build();
//                    clientVerificationRepository.save(verify);
//
//                    InstitutionConfig config = institutionConfigService.findByyApp(Application.MB.name());
//                    CustomerVerification verificationObject = institutionConfigService.countRemainingCustomerTrials(subscription,config);
//                    throw new FailedSecurityVerification(ex.getMessage(),"verificationObject");
//                }
            }
            return true;
        }
        return false;
    }
}
