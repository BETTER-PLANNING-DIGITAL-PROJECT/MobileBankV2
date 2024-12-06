package ibnk.repositories.internet;

import ibnk.models.internet.client.ClientDevice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientDeviceRepository extends JpaRepository<ClientDevice,Long> {

}
