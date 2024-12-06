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
public class AuthResponse<K,V> {
    private K UserDao;
    private V AuthInfo;



    public AuthResponse(K UserDao, V Token){
        this.UserDao = UserDao;
        this.AuthInfo =  Token;

    }
}
