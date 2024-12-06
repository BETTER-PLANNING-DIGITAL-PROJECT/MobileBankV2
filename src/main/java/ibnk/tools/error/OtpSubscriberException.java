package ibnk.tools.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.OK)

public class OtpSubscriberException extends Exception {
    private static final long serialVersionUID = 1L;

    public OtpSubscriberException(String message){
        super(message);
    }
}
