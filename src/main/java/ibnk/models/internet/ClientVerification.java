package ibnk.models.internet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ibnk.models.internet.client.Subscriptions;
import ibnk.models.internet.enums.Status;
import ibnk.models.internet.enums.VerificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Entity

public class ClientVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;
    @UuidGenerator
    @Column(unique = true, nullable = false)
    private String uuid;


    private String ip;
    private String phoneNumber;

    @JsonIgnore
    private String role;

    //    @JsonIgnore
    private String message;

    //    @JsonIgnore
    @Enumerated(EnumType.STRING)
    private VerificationType verificationType;


    @Enumerated(EnumType.STRING)
    private Status status;

    //    @JsonIgnore
    private boolean verified;

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

    @PrePersist
    public void setUuid() {
        this.uuid = String.valueOf(UUID.randomUUID());
    }
}
