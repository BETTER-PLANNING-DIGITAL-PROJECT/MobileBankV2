package ibnk.repositories.internet;

import ibnk.models.internet.EmailServer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailServerRepository extends JpaRepository<EmailServer,Long> {

    public Optional<EmailServer> findEmailServerByHost(String host);
    public Optional<EmailServer> findEmailServerByPort(Long port);
    public Optional<EmailServer> findEmailServerByInstitutionEmail(String inst);
}
