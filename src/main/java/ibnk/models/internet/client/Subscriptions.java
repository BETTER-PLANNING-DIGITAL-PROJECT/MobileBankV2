package ibnk.models.internet.client;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import ibnk.models.banking.Client;
import ibnk.models.internet.UserEntity;
import ibnk.models.internet.enums.NotificationChanel;
import ibnk.tools.Views;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Subscriptions implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @GeneratedValue(strategy = GenerationType.UUID)
    @UuidGenerator
    @Column(unique = true, nullable = false)
    private String uuid;

    private LocalDateTime subscriptionDate;

    private String branchCode;

    private String userLogin;

    private String clientName;

    private String primaryAccount;

    private String productName;

    private String clientMatricul;

    private String status;

    private String suspensionReason;

    private Boolean contactVerification;
    @ManyToOne
    @JoinColumn
    @JsonIgnore
    private UserEntity subscriberBy;

    @JsonIgnore
    private String password;
    @JsonIgnore
    @Transient
    private String passwordMb;

    @JsonIgnore
    private String pins;

    @Column(nullable = false)
    private Boolean doubleAuthentication;

    @Enumerated(EnumType.STRING)
    private NotificationChanel preferedNotificationChanel;

    @JsonIgnore
    private Boolean passExpiration;

    @JsonIgnore
    private Long passDuration;

    @JsonIgnore
    private String passPeriodicity;

    @JsonIgnore
    @JsonView(Views.UserView.class)
    private LocalDateTime passwordChangedTime;

    @JsonIgnore
    private String passwordResetRequest;


//    @Column(unique = true, nullable = false)
//    @JsonView(Views.UserView.class)
//    private String phoneNumber;

    @JsonIgnore
    @JsonView(Views.UserView.class)
    private String address;

    @Column(nullable = false, name = "first_login")
    @JsonView(Views.UserView.class)
    private Boolean firstLogin;

    @Basic(optional = false)
    @CreationTimestamp
    @Column(updatable = false)
    @JsonIgnore
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @JsonIgnore
    private LocalDateTime updatedAt;

    @Transient()
    private Client client;

    @PrePersist
    public void setUuid() {
        this.uuid = String.valueOf(UUID.randomUUID());
    }

    public boolean isPasswordExpired() {
        if (!this.passExpiration) return false;
        LocalDateTime currentTime = LocalDateTime.now();
        return currentTime.isAfter(this.passExpirationDate());
    }

    public LocalDateTime passExpirationDate() {
        LocalDateTime passExpirationDate = null;
        LocalDateTime lastChanged = this.passwordChangedTime;
        if (lastChanged == null) {
            lastChanged = this.createdAt;
        }
        switch (this.passPeriodicity) {
            case "DAY" -> passExpirationDate = lastChanged.plusDays(this.passDuration);
            case "WEEK" -> passExpirationDate = lastChanged.plusWeeks(this.passDuration);
            case "MONTH" -> passExpirationDate = lastChanged.plusMonths(this.passDuration);
        }
        return passExpirationDate;
    }

    public LocalDateTime getPassExpirationDate() {
        if (!this.passExpiration) return null;
        return this.passExpirationDate();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getUsername() {
        return this.userLogin;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public String getPhoneNumber() {
        return this.client != null ? this.client.getPhoneNumber() : null;
    }

    public String getPasswordMb() {
        return this.client != null ? this.client.getPassword() : null;
    }
    public String getEmail() {
        return this.client != null ? this.client.getEmail() : null;
    }
}
