package ibnk.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ForgotPasswordDto {
    private String newPassword;
    private String confirmPassword;
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PinDto{
        private String pin;
        private String confirmPin;
    }
}
