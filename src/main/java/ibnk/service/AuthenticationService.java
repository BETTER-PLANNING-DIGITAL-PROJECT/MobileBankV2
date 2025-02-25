package ibnk.service;

import ibnk.dto.UserDto;
import ibnk.dto.auth.AuthDto;
import ibnk.dto.auth.OtpAuth;
import ibnk.models.banking.Employee;
import ibnk.models.internet.UserEntity;
import ibnk.repositories.banking.EmployeeRepository;
import ibnk.repositories.internet.UserRepository;
import ibnk.tools.error.UnauthorizedUserException;
import ibnk.security.jwtConfig.JwtService;
import ibnk.tools.response.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor

public class AuthenticationService {
    private final UserRepository userRepository;

    private final UserService userService;
    private final OtpService otpService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmployeeRepository employeeRepository;
    private final Logger LOGGER = LogManager.getLogger(AuthenticationService.class);
    
    public AuthResponse<Object, Object> authenticate(AuthDto request) throws UnauthorizedUserException {
        LOGGER.info("Enter >> Authentication Function");
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUserLogin(), request.getPassword()));
            UserEntity user = (UserEntity) authentication.getPrincipal();

            Optional<Employee> employee = employeeRepository.findByMatricule(user.getUserLogin());
            if(employee.isEmpty()) {
                throw new UnauthorizedUserException("Unauthorized");
            }
            if(employee.get().getSuspended()) {
                throw new UnauthorizedUserException("Unauthorized");
            }

            UserDto.CreateUserDto result = UserDto.CreateUserDto.modelToDao(user);
            if (user.isPasswordExpired()) {
                user.setPasswordResetRequest("ALLOWED");
                userRepository.save(user);
                LOGGER.info("Exit1 >> Authentication Function");
                return new AuthResponse<>(result, "");
            }
            if (user.getPasswordResetRequest() != null) {
                user.setPasswordResetRequest(null);
                userRepository.save(user);
            }
            Object jwtToken;

                jwtToken = jwtService.generateToken(user);
                System.out.println("Exit3 >> Authentication Function");
                return new AuthResponse<>(user, jwtToken);

    }

    public AuthResponse<UserEntity, Object> OauthWithOtp(String guid, OtpAuth otpauth) throws  UnauthorizedUserException {
        try {
            var user = userService.loadUserByUuid(guid);
            otpService.VerifyOtp(otpauth, guid);
            Object jwtToken = jwtService.generateToken(user);
            return new AuthResponse<>(user, jwtToken);
        } catch (Exception e) {
            throw new UnauthorizedUserException(e.getMessage());
        }
    }




}