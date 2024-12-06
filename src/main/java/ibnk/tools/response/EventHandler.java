package ibnk.tools.response;

import ibnk.repositories.internet.NotificationTemplateRepository;

public class EventHandler<T> {
    private final NotificationTemplateRepository notificationTemplateRepository;

    public EventHandler(NotificationTemplateRepository notificationTemplateRepository) {
        this.notificationTemplateRepository = notificationTemplateRepository;
    }


}
