package ibnk.dto.BankingDto;

import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@Data
public class CheckbookRequestDto {
    @Data
    public static class StopPay {
        private String account;
        private String checkNumber;
        private String emissionDate;
        private String reason;
        private String denomination;
        private String oppositionDate;
        private String Agence;
        private String LibAgence;
        private String Reference;
        private String Employe;
        private String LibEmploye;
        private String blockPeriod;

        public static StopPay modelToList(StopPay stopPay, ResultSet map) throws SQLException {
            stopPay.setAccount(map.getString("CpteJumelle"));
            stopPay.setCheckNumber(map.getString("NumChequier"));
            stopPay.setEmissionDate(map.getString("DateEmission"));
            stopPay.setReason(map.getString("Description"));
            stopPay.setDenomination(map.getString("Denomination"));
            stopPay.setOppositionDate(map.getString("DateOpposition"));
            stopPay.setBlockPeriod("7");
            // Populate the StopPay object with data from the result set
            stopPay.setAgence(map.getString("Agence"));
            stopPay.setLibAgence(map.getString("LibAgence"));
//            stopPay.setReference(map.getString("Reference"));
//            stopPay.setEmploye(map.getString("Employee"));
//            stopPay.setLibEmploye(map.getString("LibEmploye"));
            return stopPay;
        }

    }

    @Data
    public static class ChequeSeries {
        private String[] chequeSeries;
        private String agence;
        private String libAgence;
        private String clientMat;
        private String clientName;
        private String description;
        private String accountId;
        private String startSeries;
        private String endSeries;

        public static ChequeSeries modelToList(ChequeSeries checkbookRequestDto, ResultSet map) throws SQLException {
            checkbookRequestDto.setStartSeries(map.getString("Dserie"));
            checkbookRequestDto.setEndSeries(map.getString("Fserie"));
            return checkbookRequestDto;
        }

        public static ChequeSeries modelToInsert(ChequeSeries checkbookRequestDto, ResultSet map) throws SQLException {
            checkbookRequestDto.setAccountId(map.getString("Cptejumellecc"));
            checkbookRequestDto.setClientName(map.getString("NomJumelle"));
            checkbookRequestDto.setLibAgence(map.getString("LibAgence"));
            checkbookRequestDto.setAgence(map.getString("Agence"));
            checkbookRequestDto.setClientMat(map.getString("cli"));
            return checkbookRequestDto;
        }
    }

    private int id;
    private String accountId;
    private int numberPages;
    private String description;
    private String rejectReason;
    private String checkBookNumber;
    private String client;
    private int lect;
    private String ErrMsg;
    private String dateCreation;
    private String etat;
    private int idCheckbook;
    private int page;

    public static CheckbookRequestDto modelToList(CheckbookRequestDto checkbookRequestDto, ResultSet map) throws SQLException {
        checkbookRequestDto.setId(map.getInt("id"));
        checkbookRequestDto.setDateCreation(map.getString("DateCreation"));
        checkbookRequestDto.setEtat(map.getString("Statut"));
        checkbookRequestDto.setPage(Integer.parseInt(map.getString("Nombrepage")));
        checkbookRequestDto.setNumberPages(Integer.parseInt(map.getString("Nombrepage")));
        checkbookRequestDto.setDescription(map.getString("Description"));
        checkbookRequestDto.setClient(map.getString("Client"));
        checkbookRequestDto.setRejectReason(map.getString("RejectReason"));
        checkbookRequestDto.setAccountId(map.getString("CpteJumelle"));
        return checkbookRequestDto;
    }

    public static CheckbookRequestDto modelToDao(Map<String, Object> map) {
        CheckbookRequestDto checkbookRequestDto = new CheckbookRequestDto();
        checkbookRequestDto.setLect((Integer) map.get("lect"));
        checkbookRequestDto.setErrMsg((String) map.get("ErrMsg"));
        checkbookRequestDto.setDateCreation((String) map.get("dateServer"));
        checkbookRequestDto.setPage((int) map.get("page"));
        checkbookRequestDto.setEtat((String) map.get("etat"));
        checkbookRequestDto.setDescription((String) map.get("description"));
        checkbookRequestDto.setClient((String) map.get("pc_Client"));
        checkbookRequestDto.setNumberPages((Integer) map.get("nombrePage"));
        checkbookRequestDto.setAccountId((String) map.get("CpteJumelle"));
        return checkbookRequestDto;
    }
}
