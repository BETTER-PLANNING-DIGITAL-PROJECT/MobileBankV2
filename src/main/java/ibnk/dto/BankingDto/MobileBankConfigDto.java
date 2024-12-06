package ibnk.dto.BankingDto;

import jakarta.annotation.Nullable;
import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@Data
public class MobileBankConfigDto {
    private String code;
    private String description;
    private Float maxPerDayToMobile;
    private int nbMaxPerDayToMobile;
    private Float maxPeMonthToMobile;
    private Float maxPeWeekToMobile;
    private Float maxAmtToMobile;
    private Float maxAmtFromMobile;
    private Float maxAmtTrfToAccount;
    private Float maxPerDayTrfToAccount;
    private Float minAmtTrfToAccount;
    private Float minAmtToMobile;
    private Float monthlyFee;
    private Float fraisAbonnement;

    public static MobileBankConfigDto modelToDaa(MobileBankConfigDto mobileBankConfigDto, Map<String, Object> map) throws SQLException {
        mobileBankConfigDto.setCode((String) map.get("Code"));
        mobileBankConfigDto.setDescription((String) map.get("Description"));
        mobileBankConfigDto.setMaxPerDayToMobile(convertToFloatOrDefault(map.get("MaxPerDayToMobile"),0f));
        mobileBankConfigDto.setNbMaxPerDayToMobile(convertToInt(map.get("NbMaxPerDayToMobile"),0));
        mobileBankConfigDto.setMaxPeMonthToMobile(convertToFloatOrDefault(map.get("MaxPerDayToMobile"),0f));
        mobileBankConfigDto.setMaxPeWeekToMobile(convertToFloatOrDefault(map.get("MaxPeWeekToMobile"),0f));
        mobileBankConfigDto.setMaxAmtToMobile(convertToFloatOrDefault(map.get("MaxAmtToMobile"),0f));
        mobileBankConfigDto.setMaxAmtFromMobile(convertToFloatOrDefault(map.get("MaxAmtFromMobile"),0f));
        mobileBankConfigDto.setMaxAmtTrfToAccount(convertToFloatOrDefault(map.get("MaxAmtTrfToAccount"),0f));
        mobileBankConfigDto.setMaxPerDayTrfToAccount(convertToFloatOrDefault(map.get("MaxPerDayTrfToAccount"),0f));
        mobileBankConfigDto.setMinAmtTrfToAccount(convertToFloatOrDefault(map.get("MinAmtTrfToAccount"),0f));
        mobileBankConfigDto.setMinAmtToMobile(convertToFloatOrDefault(map.get("MinAmtToMobile"),0f));
        mobileBankConfigDto.setMonthlyFee(convertToFloatOrDefault(map.get("MonthlyFee"),0f));
        mobileBankConfigDto.setFraisAbonnement(convertToFloatOrDefault(map.get("FraisAbonnement"), 0f));
        return mobileBankConfigDto;
    }

    private static Integer convertToInt(Object value,Integer defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return Integer.parseInt(value.toString());
    }
    private static Float convertToFloatOrDefault(Object value, Float defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return Float.valueOf(value.toString());
    }
    public static MobileBankConfigDto modelToDao(MobileBankConfigDto mobileBankConfigDto, ResultSet map) throws SQLException {
        mobileBankConfigDto.setCode(map.getString("Code"));
        mobileBankConfigDto.setDescription(map.getString("Description"));
        mobileBankConfigDto.setMaxPerDayToMobile(Float.valueOf(map.getString("MaxPerDayToMobile")));
        mobileBankConfigDto.setNbMaxPerDayToMobile(Integer.parseInt(map.getString("NbMaxPerDayToMobile")));
        mobileBankConfigDto.setMaxPeMonthToMobile(Float.valueOf(map.getString("MaxPerDayToMobile")));
        mobileBankConfigDto.setMaxPeWeekToMobile(Float.valueOf(map.getString("MaxPeWeekToMobile")));
        mobileBankConfigDto.setMaxAmtToMobile(Float.valueOf(map.getString("MaxAmtToMobile")));
        mobileBankConfigDto.setMaxAmtFromMobile(Float.valueOf(map.getString("MaxAmtFromMobile")));
        mobileBankConfigDto.setMaxAmtTrfToAccount(Float.valueOf(map.getString("MaxAmtTrfToAccount")));
        mobileBankConfigDto.setMaxPerDayTrfToAccount(Float.valueOf(map.getString("MaxPerDayTrfToAccount")));
        mobileBankConfigDto.setMinAmtTrfToAccount(Float.valueOf(map.getString("MinAmtTrfToAccount")));
        mobileBankConfigDto.setMinAmtToMobile(Float.valueOf(map.getString("MinAmtToMobile")));
        mobileBankConfigDto.setMonthlyFee(Float.valueOf(map.getString("MonthlyFee")));
        mobileBankConfigDto.setFraisAbonnement((map.getFloat("FraisAbonnement")));
        return mobileBankConfigDto;
    }
}
