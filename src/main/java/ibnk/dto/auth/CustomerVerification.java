package ibnk.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerVerification {

    private Object verificationObject;

    private String verificationType;

    private Integer trials;

    private Integer maxTrials;
}
