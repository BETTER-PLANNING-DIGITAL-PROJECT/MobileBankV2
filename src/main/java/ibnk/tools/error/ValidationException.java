package ibnk.tools.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
public class ValidationException extends Exception {
    private static final long serialVersionUID = 1L;

    public ValidationException(String message){
        super(message);
    }


}