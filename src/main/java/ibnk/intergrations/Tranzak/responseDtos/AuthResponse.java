package ibnk.intergrations.Tranzak.responseDtos;


import lombok.Data;

@Data
public class AuthResponse {
    @Data
    public static class Auth {
        String scope;
        String appId;
        String token;
        int expiresIn;
    }

    Auth data;
    String errorMsg;
    String errorCode;
    boolean success;
    String debugInfo;
}
