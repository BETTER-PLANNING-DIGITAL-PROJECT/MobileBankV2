package ibnk.models.internet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ibnk.models.internet.enums.QuestionEnum;
import ibnk.models.internet.enums.SubMethod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DynamicUpdate
public class InstitutionConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String institutionName;
    private String institutionShortName;
    private String Phone;
    private String POBOX;
    private String Town;
    private Long maxSecurityQuest;
    private Long minSecurityQuest;
    private String institutionEmail;
    private String emailPassword;
    private String returnUrl;
    private String emailNoReply;
    private String host;
    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private long PayerFeePercentage;
//    @Column(nullable = false, columnDefinition = "BIT default 0")
//    private boolean withdrawalOtp;
    @Column(unique = true)
    private String application;
    @Column(nullable = false,columnDefinition = "BIT default 0" )
    private boolean trnasOtp;
    private Long port;
    private Integer verifyQuestNumber;
    private Integer maxNumberOfAuthDevice;
    private Long maxVerifyAttempt;
    private String defaultPackage;
    private String proxy;
    @Enumerated(EnumType.STRING)
    private SubMethod subMethod;
    private Integer verificationResetTimer;
    private Long otpMinBeforeExpire;
    @Enumerated(EnumType.STRING)
    private QuestionEnum questConfig;

    @Column(nullable = false, columnDefinition = "BIT default 0")
    private boolean customerUpdateContact;

    @ManyToOne
    private Media logo;
//    @Transient
//    private Long serverId;
    @Lob
    @Column(nullable = true)
    private byte[] img;
    @ManyToOne
    @JoinColumn
    @JsonIgnore
    private EmailServer server;


    @Basic(optional = false)
    @CreationTimestamp
    @Column(updatable = false)
    @JsonIgnore
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @JsonIgnore
    private LocalDateTime updatedAt;
}
