package ibnk.tools;

import ibnk.models.internet.security.AuditLogin;
import ibnk.repositories.internet.AuditLoginRepository;
import ibnk.tools.error.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

        private final AuditLoginRepository auditLoginRepository;

    @ExceptionHandler(ResourceNotFoundException.class)
    private static ResponseEntity<?> handleNotFoundException(ResourceNotFoundException ex, HttpServletRequest  request) {
        ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ValidationException.class)
    private static ResponseEntity<?> handleValidationException(ValidationException ex, HttpServletRequest  request) {
        ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(errorDetails, HttpStatus.UNPROCESSABLE_ENTITY);
    }
    @ExceptionHandler(OtpSubscriberException.class)
    private static ResponseEntity<?> handleOtpSubscriberException(OtpSubscriberException ex, HttpServletRequest  request) {
        ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(errorDetails, HttpStatus.OK);
    }

    private static ResponseEntity<?> handleExpiredPasswordException(ExpiredPasswordException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        BindingResult result = ex.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();
        ValidationErrorResponse error = new ValidationErrorResponse(UNPROCESSABLE_ENTITY.value(), "validation error");
        for (org.springframework.validation.FieldError fieldError : fieldErrors) {
            error.addFieldError(fieldError);
        }
        return new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(UnauthorizedUserException.class)
    public ResponseEntity<?> handleUnauthorizedUserException(UnauthorizedUserException ex, HttpServletRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }

//    @ExceptionHandler(Exception.class)
//    public void handlePdfGenerationException(Exception ex, HttpServletRequest request) {
//        System.out.println(ex.getMessage());
//
//    }

    @ExceptionHandler(FailedLoginException.class)
    public ResponseEntity<?> handleFailedLoginException(FailedLoginException ex, HttpServletRequest request) {

        AuditLogin auditLogin = new AuditLogin();
        auditLogin.setLoginIp(request.getRemoteAddr());
        auditLogin.setLogin((String) request.getAttribute("login"));
        auditLogin.setLoginDevice(request.getHeader("X-CLIENT-DEVICE"));
        auditLogin.setMessage(ex.getMessage());

        auditLogin.setStatus(ex.getStatus());
        auditLogin.setLoginUuid(ex.getUserUuid());
        auditLogin.setLoginType(ex.getLoginType());

        auditLoginRepository.save(auditLogin);

        ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<?> handleUsernameNotFoundException(UsernameNotFoundException ex, HttpServletRequest request) {

        ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(FailedSecurityVerification.class)
    public ResponseEntity<?> failedSecurityVerificationExceptionHandler(FailedSecurityVerification ex, HttpServletRequest request, HttpServletResponse response) {
        ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(), request.getRequestURI(), ex.getDetails());
        return new ResponseEntity<>(errorDetails, HttpStatus.FORBIDDEN);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> globalExceptionHandler(Exception ex, HttpServletRequest request, HttpServletResponse response) {
        ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    static class ValidationErrorResponse {
        private final int status;
        private final String message;
        private final List<FieldError> fieldErrors = new ArrayList<>();

        ValidationErrorResponse(int status, String message) {
            this.status = status;
            this.message = message;
        }

        public int getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public void addFieldError(FieldError error) {
            FieldError error1 = new FieldError(error.getObjectName(), error.getField(), Objects.requireNonNull(error.getDefaultMessage()));
            fieldErrors.add(error1);
        }

        public List<FieldError> getFieldErrors() {
            return fieldErrors;
        }
    }

}
