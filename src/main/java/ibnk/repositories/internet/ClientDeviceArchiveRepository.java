package ibnk.repositories.internet;

import ibnk.models.internet.client.ClientDeviceArchive;
import ibnk.models.internet.client.ClientDeviceId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientDeviceArchiveRepository extends JpaRepository<ClientDeviceArchive, ClientDeviceId> {
}
