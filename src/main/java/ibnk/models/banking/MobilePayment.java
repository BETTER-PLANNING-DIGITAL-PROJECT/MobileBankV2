package ibnk.models.banking;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class MobilePayment {
    @Id
    @Column(name = "id", nullable = false, precision = 18)
    private BigDecimal id;

    @Size(max = 250)
    @Nationalized
    @Column(name = "Uuid", length = 250)
    private String uuid;

    @Column(name = "Montant")
    private Double amount;
    @Size(max = 20)
    @NotNull
    @Nationalized
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CpteJumelle", nullable = false)
    private Account account;

    @Size(max = 6)
    @NotNull
    @Nationalized
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "Client", nullable = false)
    private Client client;

    @Size(max = 50)
    @Nationalized
    @Column(name = "Telephone", length = 50)
    private String telephone;

    @Column(name = "Frais")
    private Double transactionCharge;

    @Size(max = 10)
    @Nationalized
    @Column(name = "Statut", length = 10)
    private String status;

    @Size(max = 255)
    @Nationalized
    @Column(name = "PaymentGatewayUuid")
    private String paymentGatewayUuid;

    @Size(max = 255)
    @NotNull
    @Nationalized
    @Column(name = "Type", nullable = false)
    private String type;

    @Column(name = "CallBackReceive")
    private Double callBackReceived;

    @Size(max = 6)
    @Nationalized
    @Column(name = "TypeOperation", length = 6)
    private String typeOperation;

    @Size(max = 30)
    @Nationalized
    @Column(name = "TrxNumber", length = 30)
    private String trxNumber;

    @Column(name = "date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime date;
    private String application;


}