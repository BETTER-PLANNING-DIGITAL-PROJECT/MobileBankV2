package ibnk.dto.BankingDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Types;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BillingListDto {
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY )
    private String Pd_ServerDate;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String Pc_PrincipAccount;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String Pc_SlaveAccount;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String Pc_TypeOp;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String Pc_CodeOp;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Double SvMontant;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String SvParaTx;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String Language;
    private String ErrMsg;
    private String currency;
    private int lect;
    private Double retAmount;
    private Double mntTva;

    public static BillingListDto modeltodto(Map<String, Object> map){
        BillingListDto billingListDto = new BillingListDto();
        billingListDto.setLect((Integer) map.get("lect"));
        billingListDto.setErrMsg((String) map.get("ErrMsg"));
        billingListDto.setMntTva( map.get("MntTva") == null ? 0 : (Double) map.get("MntTva"));
        billingListDto.setRetAmount((Double) map.get("RetAmount"));
        billingListDto.setCurrency((String) map.get("Currency"));
        return billingListDto;
    }
}
