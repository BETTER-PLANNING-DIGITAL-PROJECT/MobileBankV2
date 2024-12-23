package ibnk.repositories.internet;

import ibnk.models.internet.ClientVerificationArchive;
import ibnk.models.internet.client.ClientDeviceArchive;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientDeviceArchiveRepository extends JpaRepository<ClientDeviceArchive,Long> {
}
