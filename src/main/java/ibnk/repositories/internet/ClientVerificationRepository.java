package ibnk.repositories.internet;

import ibnk.models.internet.ClientVerification;
import ibnk.models.internet.client.Subscriptions;
import ibnk.models.internet.enums.Status;
import ibnk.models.internet.enums.VerificationType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ClientVerificationRepository extends JpaRepository<ClientVerification,Long> {
    Optional<ClientVerification> findByUuid(String uuid);

    @Query("SELECT COUNT(v.id) FROM  ClientVerification v  WHERE v.subscriptions =?1 and v.status = ?2  and v.createdAt >= ?3 and v.verificationType = ?4")
    Integer countPreviousFailedTrials(@Param("client") Subscriptions client, @Param("status") Status status, @Param("time") LocalDateTime date, @Param("type") VerificationType type);

    @Modifying
    @Transactional
    void deleteBySubscriptions(Subscriptions subs);
    List<ClientVerification> findClientVerificationBySubscriptionsAndVerificationType(Subscriptions subscriptions, VerificationType verificationType);

}
