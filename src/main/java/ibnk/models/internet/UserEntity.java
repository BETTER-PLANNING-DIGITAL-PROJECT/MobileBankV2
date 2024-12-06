package ibnk.models.internet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

//import ibnk.models.authorization.AppPaths;
import ibnk.models.internet.authorization.Roles;
import ibnk.tools.Views;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DynamicUpdate
//@Table(schema = "InternetBanking")
@JsonView(Views.UserView.class)
public class UserEntity implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @GeneratedValue(strategy = GenerationType.UUID)
    @UuidGenerator
    @Column(unique = true, nullable = false)
    @JsonView(Views.UserView.class)
    private String uuid;

    @JsonView(Views.UserView.class)
    private String name;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Roles role;

    @Column(unique = true, nullable = false)
    @JsonView(Views.UserView.class)
    private String userLogin;

    @JsonIgnore
    private String password;

    @JsonIgnore
    private String branchCode;

    @Column(nullable = false)
    @JsonView(Views.UserView.class)
    @JsonIgnore
    private Boolean doubleAuthentication;

    @JsonView(Views.UserView.class)
    @JsonIgnore
    private Boolean passExpiration;

    @JsonView(Views.UserView.class)
    private Long passDuration;

    @JsonView(Views.UserView.class)
    private String passPeriodicity;

    @JsonView(Views.UserView.class)
    private LocalDateTime passwordChangedTime;

    @JsonIgnore
    private String passwordResetRequest;


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

    @PrePersist
    public void setUuid() {
        this.uuid = String.valueOf(UUID.randomUUID());
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

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getUsername() {
        return this.userLogin;
    }

    public LocalDateTime getPassExpirationDate() {
        if (!this.passExpiration) return null;
        return this.passExpirationDate();
    }


}

