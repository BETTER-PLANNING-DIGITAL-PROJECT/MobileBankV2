package ibnk.models.internet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@DynamicUpdate
public class StopPaymentHist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String accountId;
    private String checkNum;
    private String denomination;
    private String description;
    private String agence;
    private String libAgence;
    private String employe;
    private String libEmploye;
    private String reference;

private String oppositionDate;
//    @Basic(optional = false)
//    @CreationTimestamp
//    @Column(updatable = false)
//    private String DateLeveeOpposition;
    @Basic(optional = false)
    @CreationTimestamp
    @Column(updatable = false)
    @JsonIgnore
    private LocalDateTime DateLeveeOpposition;

    @UpdateTimestamp
    @JsonIgnore
    private LocalDateTime updatedAt;
}
