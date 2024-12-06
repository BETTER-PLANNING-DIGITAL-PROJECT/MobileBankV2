package ibnk.repositories.internet;

import ibnk.models.internet.NotificationTemplate;
import ibnk.models.internet.enums.NotificationChanel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate,Long> {
    Optional<NotificationTemplate> findById(Long id);
    Optional<NotificationTemplate> findByIdAndNotificationType(Long id, NotificationChanel type);
    Optional<NotificationTemplate> findByNotificationTypeAndEventCode(NotificationChanel typeNotification, String event);

}
