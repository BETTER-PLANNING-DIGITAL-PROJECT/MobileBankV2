package ibnk.repositories.internet;

import ibnk.models.internet.client.Subscriptions;
import ibnk.models.internet.security.AuditClientPassword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuditClientPasswordRepository extends JpaRepository<AuditClientPassword, Long> {

    List<AuditClientPassword> findBySubscriber(Subscriptions subscriber);
}