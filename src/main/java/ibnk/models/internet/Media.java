package ibnk.models.internet;

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
public class Media {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @UuidGenerator
    @Column(unique = true, nullable = false)
    private String uuid;

    private String guid;

    private String role;

    private String photo;
    @Lob
    @Column(nullable = false)
    private byte[] img;

    private String fileName;

    private String originalFileName;

    private Long size;

    private String type;

    private String extension;

    @Basic(optional = false)
    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
    @PrePersist
    public void setUuid() {
        this.uuid = UUID.randomUUID().toString();
    }
}
