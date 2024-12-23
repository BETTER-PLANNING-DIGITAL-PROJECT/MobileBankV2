package ibnk.models.internet;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import ibnk.models.internet.enums.NotificationChanel;
import ibnk.models.internet.enums.OtpEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "_otp")
public class OtpEntity {
    @Id
    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @UuidGenerator
    @Column(unique = true, nullable = false)
    private String uuid;

    @Column(nullable = false)
    private String guid;

    @Column(nullable = false)
    private Long minBeforeExpire;

    @JsonIgnore
    private Long otp;
    @JsonIgnore
    private String email;

    @JsonIgnore
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private OtpEnum role;

    @JsonIgnore
    private String subject;
    @JsonIgnore
    private Boolean sent;

    @Enumerated(EnumType.STRING)
    @JsonIgnore
    private NotificationChanel transport;

    @JsonIgnore
    private Boolean used;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private OffsetDateTime expiresAt;


    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private OffsetDateTime createdAt;


    @PrePersist
    public void setUuid() {
        this.uuid = UUID.randomUUID().toString();
    }
}
