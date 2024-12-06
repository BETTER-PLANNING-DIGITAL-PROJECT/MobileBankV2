package ibnk.dto;

import ibnk.models.internet.client.Subscriptions;
import ibnk.models.internet.enums.NotificationChanel;
import lombok.Data;

import java.util.List;

@Data
public class NotificationEvent {
    private String eventCode;
    private List<Object> payload;
    private NotificationChanel type;
    private String email;
    private String phoneNumber;
    private Subscriptions subscriber;


}
