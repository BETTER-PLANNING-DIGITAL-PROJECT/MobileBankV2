package ibnk.models.internet.client;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table
public class ClientConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String accountNumber;

    private String accountName;

    private String client;
    private Long numberOfDevice;
    private Float maxAmount;
    private Float minAmount;
    private Boolean alertAmount;
    private Boolean alertTrans;
    private Float transLimit;
    @JsonIgnore
    private Boolean balanceNotificationSent;
    @JsonIgnore
    private Boolean transNotificationSent;

    @ManyToOne
    @JoinColumn
    @JsonIgnore
    private Subscriptions subscriptions;

    @Basic(optional = false)
    @CreationTimestamp
    @Column(updatable = false)
    @JsonIgnore
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @JsonIgnore
    private LocalDateTime updatedAt;
}
