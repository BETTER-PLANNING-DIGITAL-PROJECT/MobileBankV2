package ibnk.intergrations.BetaSms.ResponseDto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BetaResponse {
    @Enumerated(EnumType.STRING)
    private String type;
    private String senderId;
    private String message;
    private String destinations;
    private String externalReference;
    private String accessKey;
    private String publicKey;

}
