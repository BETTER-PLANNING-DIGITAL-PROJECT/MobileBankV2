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
import ibnk.repositories.internet.ClientVerificationRepository;
import ibnk.service.InstitutionConfigService;
import ibnk.tools.error.FailedSecurityVerification;
import ibnk.tools.error.UnauthorizedUserException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.Nullable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import reactor.util.annotation.NonNull;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class DeviceInterceptor implements HandlerInterceptor {

    private ClientDeviceRepository clientDeviceRepository;
    private ClientVerificationRepository clientVerificationRepository;
    private InstitutionConfigService institutionConfigService;

    public DeviceInterceptor(ClientDeviceRepository clientDeviceRepository, ClientVerificationRepository clientVerificationRepository, InstitutionConfigService institutionConfigService) {
        this.clientDeviceRepository = clientDeviceRepository;
        this.clientVerificationRepository = clientVerificationRepository;
        this.institutionConfigService = institutionConfigService;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            String requestURI = request.getRequestURI();
            if (requestURI == null || !requestURI.contains("auth/callBack/payment")) {
                String deviceHeader = request.getHeader("W");
                if (deviceHeader == null) {
                    deviceHeader = request.getHeader("X");
                    if (deviceHeader == null) {
                        throw new UnauthorizedUserException("");
                    }
                }
            }
            return true;
        }
        return false;
    }
}
