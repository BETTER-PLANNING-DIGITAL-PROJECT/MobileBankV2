package ibnk.webController;

import com.fasterxml.jackson.annotation.JsonView;
import ibnk.dto.auth.AuthDto;
import ibnk.dto.auth.OtpAuth;
import ibnk.service.AuthenticationService;
import ibnk.tools.ResponseHandler;
import ibnk.tools.Views;
import ibnk.tools.error.UnauthorizedUserException;
import ibnk.tools.response.AuthResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



/**
 * @author PHILF
 */
@RestController
@RequestMapping("/api/v1/admin/auth")
@RequiredArgsConstructor
@CrossOrigin
public class AuthenticationController {
    private final AuthenticationService service;
    private final ApplicationEventPublisher applicationEventPublisher;
    @CrossOrigin
    @PostMapping("/authenticate")
    @JsonView(Views.UserView.class)
    public ResponseEntity<Object> authentication(@Valid @RequestBody AuthDto auth) throws UnauthorizedUserException {
        AuthResponse<Object, Object> resource = service.authenticate(auth);
        applicationEventPublisher.publishEvent(resource);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", resource);
    }

    @CrossOrigin
    @PostMapping("/doubleAuthenticate/{guid}")
    public ResponseEntity<Object> doubleAuthentication(@PathVariable String guid, @RequestBody OtpAuth otpauth) throws UnauthorizedUserException {
        var resource = service.OauthWithOtp(guid, otpauth);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", resource);
    }



}
