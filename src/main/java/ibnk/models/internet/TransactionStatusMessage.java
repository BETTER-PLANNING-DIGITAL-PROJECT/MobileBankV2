package ibnk.models.internet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ibnk.models.internet.enums.Status;
import ibnk.models.internet.enums.TypeOperations;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
public class TransactionStatusMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private TypeOperations typeOp;
    @Column(columnDefinition="TEXT")
    private String text;
    @Enumerated(EnumType.STRING)
    private Status status;
    @Basic(optional = false)
    @JsonIgnore
    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @JsonIgnore
    private LocalDateTime updatedAt;
}
