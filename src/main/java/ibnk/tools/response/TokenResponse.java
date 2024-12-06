package ibnk.tools.response;

import com.fasterxml.jackson.annotation.JsonView;
import ibnk.tools.Views;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
@JsonView(Views.UserView.class)
public class TokenResponse <K,V, E>{
    private K Token;
    private V ExpireAt;
    private E ExpiresIn;
    public TokenResponse(K Token, V ExpireAt, E ExpiresIn){
        this.ExpireAt = ExpireAt;
        this.Token = Token;
        this.ExpiresIn = ExpiresIn;

    }
}