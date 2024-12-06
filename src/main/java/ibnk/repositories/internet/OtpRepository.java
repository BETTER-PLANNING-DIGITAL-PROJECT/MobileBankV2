package ibnk.repositories.internet;

import ibnk.models.internet.OtpEntity;
import ibnk.models.internet.enums.OtpEnum;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<OtpEntity,Long> {
    Optional<OtpEntity> findByUuid(String uuid);

    Optional<OtpEntity> findByUuidAndUsed(String uuid, Boolean used);

    Optional<OtpEntity> findByRoleAndGuidAndUsed(OtpEnum role, String guid, Boolean used);

    Optional<OtpEntity> findByRoleAndUuidAndUsed(OtpEnum role, String uuid, Boolean used);
    Optional<OtpEntity> findByRoleAndGuid(OtpEnum role, String guid);

    @Modifying
    @Transactional
    void deleteByRoleAndGuid(OtpEnum role, String guid);
    
}
