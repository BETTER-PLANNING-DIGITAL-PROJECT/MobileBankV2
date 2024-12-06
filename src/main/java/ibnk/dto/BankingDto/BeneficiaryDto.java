package ibnk.dto.BankingDto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

@Data
public class BeneficiaryDto {
    private int id;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String client;
    private String agence;
    private String benefactorAccountNumber;
    private Boolean mobile;
    private String beneficiaire;
    private String phoneNumber;
    private String uuid = String.valueOf(UUID.randomUUID());
    private String name;

    @JsonIgnore()
    private String onInstitution;

    @JsonIgnore()
    private String ErrMsg;

    @JsonIgnore()
    private int lect;

    public static BeneficiaryDto modelToDao(BeneficiaryDto beneficiaryDto, ResultSet map) throws SQLException {
        beneficiaryDto.setId(map.getInt("id"));
        beneficiaryDto.setAgence(map.getString("agence"));
        beneficiaryDto.setBeneficiaire(map.getString("beneficiaire"));
        beneficiaryDto.setBenefactorAccountNumber(map.getString("donneur"));
        beneficiaryDto.setName(map.getString("nom"));
        beneficiaryDto.setOnInstitution(map.getString("onInstitution"));
        beneficiaryDto.setPhoneNumber(map.getString("telephone"));
        return beneficiaryDto;
    }

    public static BeneficiaryDto modelToDto(Map<String, Object> map) throws SQLException {
        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setId((Integer) map.get("id"));
        beneficiaryDto.setAgence((String) map.get("agence"));
        beneficiaryDto.setBeneficiaire((String) map.get("beneficiaire"));
        beneficiaryDto.setBenefactorAccountNumber((String) map.get("donneur"));
        beneficiaryDto.setName((String) map.get("nom"));
        beneficiaryDto.setOnInstitution((String) map.get("onInstitution"));
        beneficiaryDto.setPhoneNumber((String) map.get("telephone"));
        beneficiaryDto.setLect((Integer) map.get("lect"));
        beneficiaryDto.setErrMsg((String) map.get("ErrMsg"));
        return beneficiaryDto;
    }
    public static BeneficiaryDto modelToDelete(Map<String, Object> map) throws SQLException {
        BeneficiaryDto beneficiaryDto = new BeneficiaryDto();
        beneficiaryDto.setBeneficiaire((String) map.get("account"));
        beneficiaryDto.setName((String) map.get("nom"));
        beneficiaryDto.setLect((Integer) map.get("lect"));
        beneficiaryDto.setErrMsg((String) map.get("ErrMsg"));
        return beneficiaryDto;
    }
}
