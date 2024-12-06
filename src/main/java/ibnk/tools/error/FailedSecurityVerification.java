package ibnk.tools.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class FailedSecurityVerification extends AccessDeniedException {
    private final Object details;
    public FailedSecurityVerification(String msg, Object details) {
        super(msg);
        this.details = details;
    }
}
