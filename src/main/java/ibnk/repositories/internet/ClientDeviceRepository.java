package ibnk.repositories.internet;

import ibnk.models.internet.client.ClientDevice;
import ibnk.models.internet.client.Subscriptions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientDeviceRepository extends JpaRepository<ClientDevice,Long> {
    Optional<ClientDevice> findByDeviceIdAndIsActiveAndUserId(String deviceId, Boolean isActive, Subscriptions userId);
    Optional<ClientDevice> findByDeviceId(String deviceId);
    Optional<ClientDevice> findByDeviceIdAndDeviceToken(String deviceId,String deviceTok);

    Optional<ClientDevice> findClientDeviceByUserId(Subscriptions userId);

    Optional<ClientDevice> findByUuidAndIsActiveAndUserId(String uuid, Boolean isActive, Subscriptions userId);


    List<ClientDevice> findClientDeviceByUserIdAndIsTrusted(Subscriptions userId, boolean trust);




}
