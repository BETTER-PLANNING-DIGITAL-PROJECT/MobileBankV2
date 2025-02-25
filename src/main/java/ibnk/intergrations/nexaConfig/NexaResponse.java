package ibnk.intergrations.nexaConfig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
public class NexaResponse {
    public String mobiles;
    private String sms;
    private String senderid; //'GBAGENT',
    private String user;
    private String password;
}
