package ibnk.repositories.internet;

import ibnk.models.internet.security.AuditLogin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLoginRepository extends JpaRepository<AuditLogin,Long> {
}
