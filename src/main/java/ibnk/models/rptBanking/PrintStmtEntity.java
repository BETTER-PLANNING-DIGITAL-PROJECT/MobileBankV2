package ibnk.models.rptBanking;

import ibnk.models.banking.EmployeId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.util.Arrays;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Entity(name = "PrintStmt")

//@Table(name = "PrintStmt", schema = "dbo", catalog = "rptBanking")
public class PrintStmtEntity {
    @Id
    private Long id;

    @Basic
    @Column(name = "Agence")
    private String agence;

    private String employeeId;

    @Basic
    @Column(name = "LibAgence")
    private String libAgence;
    @Basic
    @Column(name = "AcctNo")
    private String acctNo;
    @Basic
    @Column(name = "AcctName")
    private String acctName;
    @Basic
    @Column(name = "Devise")
    private String devise;
    @Basic
    @Column(name = "LibDevise")
    private String libDevise;
    @Basic
    @Column(name = "EndDate")
    private Date endDate;
    @Basic
    @Column(name = "DateValeur")
    private Date dateValeur;
    @Basic
    @Column(name = "RefOperation")
    private String refOperation;
    @Basic
    @Column(name = "Description")
    private String description;
    @Basic
    @Column(name = "DateOperation")
    private String dateOperation;
    @Basic
    @Column(name = "RepDebit")
    private Double repDebit;
    @Basic
    @Column(name = "RepCredit")
    private Double repCredit;
    @Basic
    @Column(name = "Solde")
    private Double solde;
    @Basic
    @Column(name = "MtDebit")
    private Double mtDebit;
    @Basic
    @Column(name = "MtCredit")
    private Double mtCredit;
    @Basic
    @Column(name = "BeginDate")
    private Date beginDate;
    @Basic
    @Column(name = "Sens")
    private String sens;
    @Basic
    @Column(name = "TrCode")
    private String trCode;
    @Basic
    @Column(name = "PageCount")
    private Integer pageCount;
    @Basic
    @Column(name = "Serie")
    private Object serie;
    @Basic
    @Column(name = "SerieNum")
    private Integer serieNum;
    @Basic
    @Column(name = "CompanyName")
    private String companyName;
    @Basic
    @Column(name = "RegionCountry")
    private String regionCountry;
    @Basic
    @Column(name = "Telephone")
    private String telephone;
    @Basic
    @Column(name = "Fax")
    private String fax;
    @Basic
    @Column(name = "Adresse")
    private String adresse;
    @Basic
    @Column(name = "LogoBranch")
    private byte[] logoBranch;
    @Basic
    @Column(name = "Matricule")
    private String matricule;
    @Basic
    @Column(name = "REPRESENTATIVE")
    private String representative;


}
