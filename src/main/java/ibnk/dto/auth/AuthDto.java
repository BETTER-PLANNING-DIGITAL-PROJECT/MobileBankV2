package ibnk.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthDto  {
    @NotNull(message = "user was not provided")
    private String userLogin;
    @NotNull(message = "password was not provided", payload = {}, groups = {})
    private String password;

}

