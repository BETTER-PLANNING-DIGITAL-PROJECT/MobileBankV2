package ibnk.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePasswordDto {
    private String oldPassword ;

    private String  newPassword;

    private String  confirmPassword;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdatePinDto{
        private String oldPin;
        private String  newPin;
        private String confirmPin;
    }
}
