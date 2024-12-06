package ibnk.tools.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class UnauthorizedUserException extends Exception {
    public UnauthorizedUserException(String message){
        super(message);
    }

}
