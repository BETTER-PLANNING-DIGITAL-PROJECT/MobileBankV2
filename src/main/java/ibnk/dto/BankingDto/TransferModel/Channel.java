package ibnk.dto.BankingDto.TransferModel;

import ibnk.models.internet.enums.ChannelCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Channel {
    private ChannelCode code;
}
