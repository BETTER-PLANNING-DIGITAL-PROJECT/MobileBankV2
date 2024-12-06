package ibnk.models.banking;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;



@Getter
@Setter
@Entity
public class MobileBankConfiguration {
    @Id
    @Size(max = 50)
    @Nationalized
    @Column(name = "Code", nullable = false, length = 50)
    private String code;

    @Size(max = 50)
    @NotNull
    @Nationalized
    @Column(name = "Description", nullable = false, length = 50)
    private String description;

    @Column(name = "MaxPerDayToMobile")
    private Double maxPerDayToMobile;

    @Column(name = "NbMaxPerDayToMobile")
    private Integer nbMaxPerDayToMobile;

    @Column(name = "MaxPeMonthToMobile")
    private Double maxPeMonthToMobile;

    @Column(name = "MaxPeWeekToMobile")
    private Double maxPeWeekToMobile;

    @Column(name = "MaxAmtToMobile")
    private Double maxAmtToMobile;

    @Column(name = "MaxAmtFromMobile")
    private Double maxAmtFromMobile;

    @Column(name = "MaxAmtTrfToAccount")
    private Double maxAmtTrfToAccount;

    @Column(name = "MaxPerDayTrfToAccount")
    private Double maxPerDayTrfToAccount;

    @Column(name = "MinAmtTrfToAccount")
    private Double minAmtTrfToAccount;

    @Column(name = "MinAmtToMobile")
    private Double minAmtToMobile;

    @Column(name = "MonthlyFee")
    private Double monthlyFee;

    @Column(name = "FraisAbonnement")
    private Double subscriptionFee;


}