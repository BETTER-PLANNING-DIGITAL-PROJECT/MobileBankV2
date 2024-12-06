package ibnk.models.internet.client;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ibnk.models.internet.Media;
import ibnk.models.internet.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table
public class ClientRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @GeneratedValue(strategy = GenerationType.UUID)
    @UuidGenerator
    @Column(unique = true, nullable = false)
    private String uuid;
    private String name;
    private String surName;
    private String fullName;
    private String fatherName;
    private String motherName;
    private String countryOfResidence;
    private String birthDate;
    private String profession;
    private String nationality;
    private String customerType;
    private String gender;
    private String accountType;
    private String maritalStatus;

    //CONTACT INFO'S APPLY TO BOTH
    private String email;
    private String poBox;
    private String address1;
    private String address2;
    private String phoneNumber1;
    private String phoneNumber2;
    private String longitude;
    private String latitude;


    private boolean contactVerification;
    private String identificationType;
    private String identificationNumber;
    private String matNumber;
    private String idIssueDate;
    private String idExpDate;
    @OneToOne
    @JoinColumn
    @JsonIgnore
    private Media photo;

    @OneToOne
    @JoinColumn
    @JsonIgnore
    private Media frontIdentification;

    @OneToOne
    @JoinColumn
    @JsonIgnore
    private Media backIdentification;



    private String ptcName;
    private String ptcAddress;
    private String ptcPhoneNumber;
    private String ptcNic;

    //
    @OneToOne
    @JoinColumn
    private UserEntity verifiedBy;
    private String status;
    private String comment;


    //Moral
    private String taxPayerNum;
    private String initials;
    private String socialObj;
    private String headQuater;
    private String licenseNum;
    private String judicialNat;
    private String businessmanager;
    private String dateOfCreation;





    @PrePersist
    public void setUuid() {
        this.uuid = String.valueOf(UUID.randomUUID());
    }

    @Basic(optional = false)
    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;


    @UpdateTimestamp
    private LocalDateTime updatedAt;




}
