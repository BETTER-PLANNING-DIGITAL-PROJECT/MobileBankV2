package ibnk.models.internet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ibnk.models.internet.enums.NotificationChanel;
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
public class NotificationTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventCode;

    private String status;

    @Column(columnDefinition="TEXT")
    private String template;

    private String subject;

    @Enumerated(EnumType.STRING)
    private NotificationChanel notificationType;

    @Basic(optional = false)
    @CreationTimestamp
    @Column(updatable = false)
    @JsonIgnore
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @JsonIgnore
    private LocalDateTime updatedAt;
}
