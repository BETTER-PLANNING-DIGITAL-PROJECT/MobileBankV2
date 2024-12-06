package ibnk.models.banking;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "devise")
public class Currency {
    @Id
    @Size(max = 3)
    @Nationalized
    @Column(name = "devcod", nullable = false, length = 3)
    private String code;

    @Size(max = 10)
    @Nationalized
    @Column(name = "devsign", length = 10)
    private String shortName;

    @Column(name = "devdate")
    private LocalDateTime devdate;

    @Size(max = 3)
    @Nationalized
    @Column(name = "CurrentCurrency", length = 3)
    private String isCurrent;

}