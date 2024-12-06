package ibnk.tools.error;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@EqualsAndHashCode(callSuper = true)
@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class FailedLoginException extends AuthenticationException {
    private final String userUuid;
    private final String status;
    private final String loginType;

    public FailedLoginException(String message, String userUuid, String status, String loginType) {
        super(message);
        this.userUuid = userUuid;
        this.status = status;
        this.loginType = loginType;
    }

    public String getUserUuid() {
        return status;
    }
}
