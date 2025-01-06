package ibnk.repositories.internet;

import ibnk.models.internet.client.ClientDevice;
import ibnk.models.internet.client.ClientDeviceId;
import ibnk.models.internet.client.Subscriptions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientDeviceRepository extends JpaRepository<ClientDevice, ClientDeviceId> {

    @Query("SELECT e FROM ClientDevice e WHERE e.id.userId.uuid = :subscriberUuid")
    List<ClientDevice> findByUserUuid(@Param("subscriberUuid") String uuid);

    @Query("SELECT e FROM ClientDevice e WHERE e.id.deviceId = :deviceId")
    Optional<ClientDevice> findByUserDeviceId(@Param("deviceId") String deviceId);


}
