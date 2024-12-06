package ibnk.webController;

import ibnk.dto.auth.OtpAuth;
import ibnk.models.internet.enums.OtpEnum;
import ibnk.tools.ResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author PHILF
 */
@RestController
@RequestMapping("/api/v1/admin/auth/otp")
@RequiredArgsConstructor
@CrossOrigin
public class OtpController {

//    @GetMapping("/resend/{uuid}")
//    public ResponseEntity<Object> ResendOtp(@PathVariable(value = "uuid") String uuid) throws ResourceNotFoundException {
//        var result = otpService.ResendOtp(uuid);
//        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", result);
//    }


    @PostMapping("/VerifyOtp/{guid}")
    public ResponseEntity<Object> verifyOtpResetPassword(@PathVariable(value = "guid") String guid, OtpAuth otp)  {
        var result = false;
        switch (OtpEnum.valueOf(otp.getRole())){

        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Success", result);
    }
}

