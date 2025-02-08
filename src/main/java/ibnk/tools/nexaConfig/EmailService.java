package ibnk.tools.nexaConfig;

import ibnk.dto.NotificationEvent;
import ibnk.intergrations.BetaSms.BetaSmsService;
import ibnk.intergrations.BetaSms.ResponseDto.BetaResponse;
import ibnk.models.internet.InstitutionConfig;
import ibnk.models.internet.NotificationTemplate;
import ibnk.models.internet.enums.Application;
import ibnk.models.internet.enums.NotificationChanel;
import ibnk.repositories.internet.NotificationTemplateRepository;
import ibnk.service.InstitutionConfigService;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.mail.MailException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import static ibnk.service. OtpService.replaceParameters;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final InstitutionConfigService institutionConfigService;
    private final BetaSmsService betaSmsService;
    private final NotificationTemplateRepository notificationTemplateRepository;

    public EmailService(InstitutionConfigService institutionConfigService, BetaSmsService betaSmsService, NotificationTemplateRepository notificationTemplateRepository) {
        this.institutionConfigService = institutionConfigService;
        this.betaSmsService = betaSmsService;
        this.notificationTemplateRepository = notificationTemplateRepository;
    }

    public Session configure(InstitutionConfig inst)  {
        // Fetch email configurations from the database
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.enable", "true"); // Enable SSL
        props.put("mail.debug", "true");
        props.put("mail.smtp.host", inst.getHost());
        props.put("mail.smtp.port", Math.toIntExact(inst.getPort()));
        props.put("jakarta.mail.util.StreamProvider", "org.eclipse.angus.mail.util.MailStreamProvider");
        return Session.getDefaultInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(inst.getInstitutionEmail(), inst.getEmailPassword());
            }
        });
    }

    @Async
    public void sendSimpleMessage(String to, String subject, String text) {
        try {
            InstitutionConfig inst = institutionConfigService.findByyApp(Application.MB.name());
            Session session = configure(inst);
            session.setProtocolForAddress("rfc822", "smtp");

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(inst.getInstitutionEmail(), "Internet-Banking"));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);
            message.setContent(text,"text/html");
            Transport.send(message);
            log.info("Mail send successfully");
        } catch (MessagingException | MailException e) {
            log.error("Error sending email", e);
            CompletableFuture.completedFuture("Error sending email: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error occurred", e);
            CompletableFuture.completedFuture("Unexpected error occurred: " + e.getMessage());
        }
    }

    @Async
    public void sendSimpleMessageAttach(String to, String subject, String text, String acc, byte[] pdfData) {
        try {
            InstitutionConfig inst = institutionConfigService.findByyApp(Application.MB.name());
            Session session = configure(inst);
            session.setProtocolForAddress("rfc822", "smtp");

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(inst.getInstitutionEmail(), "Internet-Banking"));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);

            // Create the text part
            MimeBodyPart textBodyPart = new MimeBodyPart();
            textBodyPart.setText(text);

            // Create the HTML part
            MimeBodyPart htmlBodyPart = new MimeBodyPart();
            htmlBodyPart.setContent(text, "text/html");

            // Create a multipart message for text and HTML
            MimeMultipart multipartAlternative = new MimeMultipart("alternative");
            multipartAlternative.addBodyPart(textBodyPart);
            multipartAlternative.addBodyPart(htmlBodyPart);

            // Create the alternative part and add to multipart/mixed
            MimeBodyPart alternativeBodyPart = new MimeBodyPart();
            alternativeBodyPart.setContent(multipartAlternative);

            // Create the attachment part
            MimeBodyPart attachmentBodyPart = new MimeBodyPart();
            DataSource source = new ByteArrayDataSource(pdfData, "application/pdf");
            attachmentBodyPart.setDataHandler(new DataHandler(source));
            attachmentBodyPart.setFileName(acc + ".pdf");

            // Create the final multipart message
            MimeMultipart multipartMixed = new MimeMultipart("mixed");
            multipartMixed.addBodyPart(alternativeBodyPart);
            multipartMixed.addBodyPart(attachmentBodyPart);

            // Set the content of the message
            message.setContent(multipartMixed);

            // Send the message
            Transport.send(message);
            log.info("Mail sent successfully");
        } catch (MessagingException | MailException e) {
            log.error("Error sending email", e);
            CompletableFuture.completedFuture("Error sending email: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error occurred", e);
            CompletableFuture.completedFuture("Unexpected error occurred: " + e.getMessage());
        }
    }


    @EventListener
    @Async
    public void handleCustomEvent(Object event) {

        if (event instanceof NotificationEvent notification) {
            String smsMessage = "";

            String mailMessage = "";
            String mailSubject = "";

            if(notification.getType().equals(NotificationChanel.BOTH) ) {
                Optional<NotificationTemplate> smsTemplate = notificationTemplateRepository.findByNotificationTypeAndEventCode(NotificationChanel.SMS, String.valueOf(notification.getEventCode()));
                Optional<NotificationTemplate> emailTemplate = notificationTemplateRepository.findByNotificationTypeAndEventCode(NotificationChanel.MAIL, String.valueOf(notification.getEventCode()));

                if(smsTemplate.isPresent() && smsTemplate.get().getStatus().equals("ACTIVE")) {
                    smsMessage = replaceParameters(smsTemplate.get().getTemplate(), notification.getPayload());
                    var betaSms = BetaResponse.builder().message(smsMessage).destinations(notification.getPhoneNumber()).type("sms").build();
                    betaSmsService.sendSms(betaSms);

                }

                if(emailTemplate.isPresent() && emailTemplate.get().getStatus().equals("ACTIVE")) {
                    mailMessage = replaceParameters(emailTemplate.get().getTemplate(), notification.getPayload());
                    mailSubject = replaceParameters(emailTemplate.get().getSubject(), notification.getPayload());
                    sendSimpleMessage(notification.getEmail(), mailSubject, mailMessage);
                }

            } else {
                Optional<NotificationTemplate> template = notificationTemplateRepository.findByNotificationTypeAndEventCode(notification.getType(), notification.getEventCode());

                if(template.isPresent() && template.get().getStatus().equals("ACTIVE")) {
                    if(notification.getType().equals(NotificationChanel.MAIL)) {
                        mailMessage = replaceParameters(template.get().getTemplate(), notification.getPayload());
                        mailSubject = replaceParameters(template.get().getSubject(), notification.getPayload());

                        sendSimpleMessage(notification.getEmail(), mailSubject, mailMessage);
                    } else if (notification.getType().equals(NotificationChanel.SMS)) {
                        smsMessage = replaceParameters(template.get().getTemplate(), notification.getPayload());
                        var betaSms = BetaResponse.builder().message(smsMessage).destinations(notification.getPhoneNumber()).type("sms").build();
                        betaSmsService.sendSms(betaSms);
                    }
                }
            }
        }

    }
}
