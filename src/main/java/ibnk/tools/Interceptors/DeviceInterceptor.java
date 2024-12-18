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

    //    @Override
//    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception{
//        String deviceHeader = request.getHeader("W");
//        ObjectMapper objectMapper = new ObjectMapper();
//
//        // Handle unauthenticated endpoints like login or resetPassword
//        if (deviceHeader != null) {
//            try {
//                ClientDevice deviceInfo = objectMapper.readValue(deviceHeader, ClientDevice.class);
//
//                // Check if the device exists
//                Optional<ClientDevice> existingDevice = clientDeviceRepository.findByDeviceId(deviceInfo.getDeviceId());
//                if (existingDevice.isEmpty()) {
//                    // Add a new device if it doesn't exist
//                    deviceInfo.setIsActive(true);
//                    deviceInfo.setIsTrusted(false); // Default to untrusted
//                    deviceInfo.setLastLoginTime(LocalDateTime.now());
//                    clientDeviceRepository.save(deviceInfo);
//                } else {
//                    // Optionally, update existing device details
//                    ClientDevice device = existingDevice.get();
//                    device.setLastLoginTime(LocalDateTime.now());
//                    clientDeviceRepository.save(device);
//                }
//            } catch (Exception e) {
//                throw new UnauthorizedUserException("Invalid device information provided");
//            }
//        }
//    }
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            String deviceHeader = request.getHeader("W");
            if(deviceHeader == null) {
                deviceHeader = request.getHeader("X");
                if(deviceHeader == null) {
                    throw new UnauthorizedUserException("");
                }
            }
//
////             Handle unauthenticated endpoints like login or resetPassword
//            if (deviceHeader != null) {
//                try {
//                    ClientDevice deviceInfo = objectMapper.readValue(deviceHeader, ClientDevice.class);
//
//                    // Check if the device exists
//                    Optional<ClientDevice> existingDevice = clientDeviceRepository.findByDeviceId(deviceInfo.getDeviceId());
//                    if (existingDevice.isEmpty()) {
//                        // Add a new device if it doesn't exist
//                        deviceInfo.setIsActive(true);
//                        deviceInfo.setIsTrusted(false); // Default to untrusted
//                        deviceInfo.setLastLoginTime(LocalDateTime.now());
//                        clientDeviceRepository.save(deviceInfo);
//                    } else {
//                        // Optionally, update existing device details
//                        ClientDevice device = existingDevice.get();
//                        device.setLastLoginTime(LocalDateTime.now());
//                        clientDeviceRepository.save(device);
//                    }
//                } catch (Exception ex) {
//                    throw new UnauthorizedUserException("Unauthorized_User");
//                }
//            }
//
////             Handle authenticated endpoints
//            Subscriptions subscription = null;
//            try {
//                subscription = (Subscriptions) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//            } catch (Exception ignored) {
//
//                // No authentication context available
//            }
//
//
//            if (subscription != null) {
//                String device_X = request.getHeader("X");
//                if (device_X != null) {
//                    ClientDevice deviceInfo = objectMapper.readValue(device_X, ClientDevice.class);
//
//                    Optional<ClientDevice> existingDevice = clientDeviceRepository.findByDeviceIdAndIsActiveAndUserId(
//                            deviceInfo.getDeviceId(), true, subscription.getUuid()
//                    );
//
//                    if (existingDevice.isPresent()) {
//                        ClientDevice device = existingDevice.get();
//                        if (!device.getIsTrusted()) {
//                            device.setIsTrusted(true);
//                            device.setLastLoginTime(LocalDateTime.now());
//                            clientDeviceRepository.save(device);
//                        }
//                    }
//
//                }
//            }
//
            return true;
        }
        return false;
    }


//    @Override
//    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
//        if (handler instanceof HandlerMethod) {
//            Subscriptions subscription = (Subscriptions) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//            try {
//                // Extract the device information from the header
//                String device_X = request.getHeader("X");
//                ObjectMapper objectMapper = new ObjectMapper();
//
//                // Handle unauthenticated endpoints like login or resetPassword
//                if (device_X != null) {
//                    try {
//                        ClientDevice deviceInfo = objectMapper.readValue(device_X, ClientDevice.class);
//                        ClientDevice deviceInfo = objectMapper.readValue(deviceHeader, ClientDevice.class);
//
//                        // Check if the device exists
//                        Optional<ClientDevice> existingDevice = clientDeviceRepository.findByDeviceId(deviceInfo.getDeviceId());
//                        if (existingDevice.isEmpty()) {
//                            // Add a new device if it doesn't exist
//                            deviceInfo.setIsActive(true);
//                            deviceInfo.setIsTrusted(false); // Default to untrusted
//                            deviceInfo.setLastLoginTime(LocalDateTime.now());
//                            clientDeviceRepository.save(deviceInfo);
//                        } else {
//                            // Optionally, update existing device details
//                            ClientDevice device = existingDevice.get();
//                            device.setLastLoginTime(LocalDateTime.now());
//                            clientDeviceRepository.save(device);
//                        }
//                    } catch (Exception ex) {
//                        throw new UnauthorizedUserException("Invalid device information provided");
//                    }
//                }
//
//                if (subscription == null) {
//                    ClientVerification verify = ClientVerification.builder()
//                            .subscriptions(subscription)
//                            .status(Status.FAILED)
//                            .verified(false)
//                            .verificationType(VerificationType.PIN)
//                            .message("Failed_Authentication")
//                            .build();
//                    clientVerificationRepository.save(verify);
//
//                    InstitutionConfig config = institutionConfigService.findByyApp(Application.MB.name());
//                    CustomerVerification verificationObject = institutionConfigService.countRemainingCustomerTrials(subscription, config, VerificationType.USER);
//                    throw new FailedSecurityVerification("Failed_Authentication", verificationObject);
//                }
//
//                // Check if the device exists
//                Optional<ClientDevice> existingDevice = clientDeviceRepository.findByDeviceIdAndIsActiveAndUserId(
//                        deviceInfo.getDeviceId(), true, subscription.getUuid()
//                );
//
//                if (existingDevice.isEmpty()) {
//                    // Add a new device if it doesn't exist
//                    deviceInfo.setUserId(subscription.getUuid());
//                    deviceInfo.setIsActive(true);
//                    deviceInfo.setIsTrusted(false);
//                    deviceInfo.setLastLoginTime(LocalDateTime.now());
//
//                    // Save the new device
//                    clientDeviceRepository.save(deviceInfo);
//
//                    // Log or record the addition
//                    ClientVerification verify = ClientVerification.builder()
//                            .subscriptions(subscription)
//                            .status(Status.SUCCESS)
//                            .verified(true)
//                            .verificationType(VerificationType.DEVICE)
//                            .message("Device added successfully")
//                            .build();
//                    clientVerificationRepository.save(verify);
//                } else {
//                    // Update trust status if conditions are met
//                    ClientDevice device = existingDevice.get();
//                    if (!device.getIsTrusted()) {
//                        device.setIsTrusted(true); // Set as trusted
//                        device.setLastLoginTime(LocalDateTime.now());
//                        clientDeviceRepository.save(device); // Save the updated device
//                    }
//                }
//
//            } catch (Exception ex) {
//                throw new UnauthorizedUserException(ex.getMessage());
//            }
//            return true;
//        }
//        return false;
//    }
//    @Override
//    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
//        if (handler instanceof HandlerMethod handlerMethod) {
//            Method method = handlerMethod.getMethod();
//            if (method.isAnnotationPresent(DeviceExtractor.class)) {
//                Subscriptions subscription = (Subscriptions) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//                try {
//                    String device = request.getHeader("X");
//                    ObjectMapper objectMapper = new ObjectMapper();
//                    ClientDevice deviceInfo = objectMapper.readValue(device, ClientDevice.class);
//
//                    if (subscription == null) {
//                        ClientVerification verify = ClientVerification.builder()
//                                .subscriptions(subscription)
//                                .status(Status.FAILED)
//                                .verified(false)
//                                .verificationType(VerificationType.PIN)
//                                .message("Failed_Authentication")
//                                .build();
//                        clientVerificationRepository.save(verify);
//
//                        InstitutionConfig config = institutionConfigService.findByyApp(Application.MB.name());
//                        CustomerVerification verificationObject = institutionConfigService.countRemainingCustomerTrials(subscription, config, VerificationType.USER);
//                        throw new FailedSecurityVerification("Failed_Authentication", verificationObject);
//                    }
//
//                    Optional<ClientDevice> info = clientDeviceRepository.findByDeviceIdAndIsActiveAndUserId(
//                            deviceInfo.getDeviceId(), true, subscription.getUuid()
//                    );
//
//                    if (info.isEmpty()) {
//                        // Set additional device info if necessary
//                        deviceInfo.setUserId(subscription.getUuid());
//                        deviceInfo.setIsActive(true);
//                        deviceInfo.setIsTrusted(false);
//                        deviceInfo.setLastLoginTime(LocalDateTime.now());
//
//                        // Save the new device
//                        clientDeviceRepository.save(deviceInfo);
//
//                        // Log or record the addition
//                        ClientVerification verify = ClientVerification.builder()
//                                .subscriptions(subscription)
//                                .status(Status.SUCCESS)
//                                .verified(true)
//                                .verificationType(VerificationType.DEVICE)
//                                .message("Device added successfully")
//                                .build();
//                        clientVerificationRepository.save(verify);
//                    }
//
//                } catch (Exception ex) {
//                    throw new UnauthorizedUserException(ex.getMessage());
//                }
//            }
//            return true;
//        }
//        return false;
//    }
//    @Override
//    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
//        if (handler instanceof HandlerMethod handlerMethod) {
//            Method method = handlerMethod.getMethod();
//            if (method.isAnnotationPresent(DeviceExtractor.class)) {
//                Subscriptions subscription = (Subscriptions) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//                try {
//                    String device = request.getHeader("X");
////                    if (device == null) {
////                        throw new UnauthorizedUserException("Required_Device");
////                    }
//                    ObjectMapper objectMapper = new ObjectMapper();
//                    ClientDevice deviceInfo = objectMapper.readValue(device, ClientDevice.class);
//                    if(subscription == null){
//                                            ClientVerification verify = ClientVerification.builder()
//                            .subscriptions(subscription)
//                            .status(Status.FAILED)
//                            .verified(false)
//                            .verificationType(VerificationType.PIN)
//                            .message("Failed_Authentication")
//                            .build();
//                    clientVerificationRepository.save(verify);
//
//                    InstitutionConfig config = institutionConfigService.findByyApp(Application.MB.name());
//                    CustomerVerification verificationObject = institutionConfigService.countRemainingCustomerTrials(subscription,config,VerificationType.USER);
//                    throw new FailedSecurityVerification("Failed_Authentication",verificationObject);
//                    }
//                   Optional<ClientDevice> info = clientDeviceRepository.findByDeviceIdAndIsActiveAndUserId(deviceInfo.getDeviceId(),true,subscription.getUuid());
////                    {
////
////                            "deviceId": "DEVICE12345",
////                            "deviceName": "John's iPhone",
////                            "deviceType": "MOBILE",
////                            "osName": "iOS",
////                            "osVersion": "15.2",
////                            "appVersion": "1.0.0",
////                            "browserName": "Safari",
////                            "browserVersion": "15.1",
////                            "deviceToken": "abcd1234efgh5678",
////
////                            "lastLoginTime": "2024-12-11T10:15:30",
////                            "isTrusted": true,
////                            "isActive": true,
////
////
////                            "latitude": 37.7749,
////                            "longitude": -122.4194
////                    }
//                    if(info.isEmpty()){
//                        ClientVerification verify = ClientVerification.builder()
//                            .subscriptions(subscription)
//                            .status(Status.FAILED)
//                            .verified(false)
//                            .verificationType(VerificationType.DEVICE)
//                            .message("Device Does not exist for this User Add it")
//                            .build();
//                    clientVerificationRepository.save(verify);
//                    }
//                    clientDeviceRepository.save(deviceInfo);
////                    String  encodedPin = subscription.getPins();
////                    if(!passwordEncoder.matches(rawPin,encodedPin)){
////                        throw new FailedSecurityVerification("pin incorrect",null);
////                    }
//                }catch (Exception ex){
//                    throw new UnauthorizedUserException(ex.getMessage());
//                }
////                catch (Exception ex) {
////                    ClientVerification verify = ClientVerification.builder()
////                            .subscriptions(subscription)
////                            .status(Status.FAILED)
////                            .verified(false)
////                            .verificationType(VerificationType.PIN)
////                            .message(ex.getMessage())
////                            .build();
////                    clientVerificationRepository.save(verify);
////
////                    InstitutionConfig config = institutionConfigService.findByyApp(Application.MB.name());
////                    CustomerVerification verificationObject = institutionConfigService.countRemainingCustomerTrials(subscription,config);
////                    throw new FailedSecurityVerification(ex.getMessage(),"verificationObject");
////                }
//            }
//            return true;
//        }
//        return false;
//    }
}
