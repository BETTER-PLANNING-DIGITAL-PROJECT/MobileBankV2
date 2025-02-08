package ibnk.tools;

import ibnk.dto.BankingDto.AccountMvtDto;
import ibnk.dto.BankingDto.PaymentDto;
import ibnk.dto.BankingDto.TransferModel.AccountCallback;
import ibnk.dto.BankingDto.TransferModel.MobilePayment;
import ibnk.models.internet.NotificationTemplate;
import ibnk.models.internet.client.ClientConfig;
import ibnk.models.internet.client.Subscriptions;
import ibnk.repositories.banking.MobilePaymentRepository;
import ibnk.repositories.internet.NotificationTemplateRepository;
import ibnk.repositories.internet.SubscriptionRepository;
import ibnk.service.BankingService.AccountService;
import ibnk.service.BankingService.MobilePaymentService;
import ibnk.service.BankingService.PaymentService;
import ibnk.tools.error.ResourceNotFoundException;
import ibnk.tools.nexaConfig.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import java.sql.SQLException;
import java.text.BreakIterator;
import java.text.SimpleDateFormat;
import java.util.*;

import static ibnk.service.OtpService.replaceParameters;

@Component
@RequiredArgsConstructor
public class ScheduleEvent {
    private final MobilePaymentService mobilePaymentService;
    private final EmailService emailService;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Value("${spring.tranzak.enviroment}")
    String ENV;


    //    2 * 60 * 60 * 1000
//    900000
//    @Scheduled(fixedRate = 3 * 60 * 1000)
    @Transactional
    public void ClientNotificationConfig() throws ResourceNotFoundException, SQLException {

//        List<ClientConfig> clientConfig = clientConfigRepository.findAll();
//        for (ClientConfig config : clientConfig) {
//
//            List<Object> payload = new ArrayList<>();
//            payload.add(config);
//
//            if (config.getAlertAmount()) {
//                AccountBalanceDto accountBalanceDto = accountService.findAccountBalances(config.getAccountNumber());
//                payload.add(accountBalanceDto);
//                if (accountBalanceDto.getAvailableBalance() >= config.getMaxAmount() && !config.getBalanceNotificationSent()) {
//                    notification(config, NotificationCode.MAX_BALANCE_ALERT.name(), payload);
//
//                    config.setBalanceNotificationSent(true);
//                    clientConfigRepository.save(config);
//
//                } else if (config.getMinAmount() < accountBalanceDto.getAvailableBalance() &&
//                        accountBalanceDto.getAvailableBalance() < (config.getMaxAmount()) &&
//                        config.getBalanceNotificationSent()) {
//                    config.setBalanceNotificationSent(false);
//                    clientConfigRepository.save(config);
//                } else if (accountBalanceDto.getAvailableBalance() <= config.getMinAmount() && !config.getBalanceNotificationSent()) {
//                    notification(config, NotificationCode.MIN_BALANCE_ALERT.name(), payload);
//                    config.setBalanceNotificationSent(true);
//                    clientConfigRepository.save(config);
//                } else if (config.getMaxAmount() > accountBalanceDto.getAvailableBalance() &&
//                        accountBalanceDto.getAvailableBalance() > config.getMinAmount() &&
//                        config.getBalanceNotificationSent()) {
//                    config.setBalanceNotificationSent(false);
//                    clientConfigRepository.save(config);
//                }
//            }
//            if (config.getAlertTrans()) {
//                Float count = accountService.Transactions(config.getAccountNumber());
//                Map<String, Object> transactionCounts = new HashMap<>();
//                transactionCounts.put("transactionCount", count);
//                payload.add(transactionCounts);
//                if (count >= config.getTransLimit() && !config.getTransNotificationSent()) {
//                    notification(config, NotificationCode.MAX_ACCOUNT_TRANSACTION.name(), payload);
//                    config.setTransNotificationSent(true);
//                    clientConfigRepository.save(config);
//                } else if (count <= config.getTransLimit() &&
//                        config.getTransNotificationSent()) {
//                    config.setTransNotificationSent(false);
//                    clientConfigRepository.save(config);
//                }
//
//            }
//        }
    }

//    @Scheduled(cron = "0 * * * * *")
//    public void transactionStatus(){
//        List<ibnk.dto.BankingDto.TransferModel.MobilePayment> mobpay = mobilePaymentService.findByStatusAndApplication("PENDING", "InternetBanking");
//        for (MobilePayment mob : mobpay) {
//            try {
//                PaymentDto dto = paymentService.transactionStatus(mob.PaymentGatewaysUuid);
//                String status = dto.getData().getTransaction().getStatus().toString();
//                if (status.trim().equals("PENDING")) {
//                    mobilePaymentService.updateMobilePayment(mob);
//                } else if (status.trim().equals("FAILED")) {
//                    if ((status.trim().equals("CANCELLED")) && mob.getType().trim().equals("WITHDRAWAL")) {
//                        AccountCallback repay = new AccountCallback();
//                        repay.setAccount(mob.getCpteJumelle());
//                        repay.setTrxNumber(mob.getTrxNumber());
//                        repay.setTelephone(mob.getTelephone());
//
//                        mobilePaymentService.account_callback(repay);
//                        mob.setCallBackReceive(true);
//
//                        mobilePaymentService.updateMobilePayment(mob);
//                    }
//                    mob.setCallBackReceive(true);
//                    mobilePaymentService.updateMobilePayment(mob);
//                    break;
//                } else if (status.trim().equals("SUCCESS")) {
//                    if (mob.Type.trim().equals("DEPOSIT")) {
//                        AccountMvtDto transfer = new AccountMvtDto();
//                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
//                        String date = simpleDateFormat.format(new Date());
//                        transfer.setAccountId(mob.getCpteJumelle());
//                        transfer.setPhoneNumber(Integer.parseInt(mob.getTelephone()));
//                        transfer.setAmount(mob.getMontant());
//                        transfer.setDate(date);
//                        transfer.setIds("");
//                        transfer.setTypeOp(mob.getTypeOperation());
//
//                        transfer.setSens(1);
//                        transfer.setDescription(
//                                dto.getData().getTransaction()
//                                        .getPayment_method().getName().concat(" Deposit from ").
//                                        concat(dto.getData().getTransaction().getRecipient()));
//
//                        AccountMvtDto item = mobilePaymentService.account_mvt(transfer);
//                        mob.TrxNumber = item.getPc_OutID();
//                        if (item.getPc_OutLECT() != 0) {
//                            break;
//                        }
//                    }
//                    mob.setCallBackReceive(true);
//                    mobilePaymentService.updateMobilePayment(mob);
//                    break;
//                }
//            }catch (Exception e){
//                e.printStackTrace();
//                System.out.println(e.getMessage());
//            }
//        }
//    }

//    @Scheduled(cron = "0 * * * * *") // Runs every minute
//    public void transactionStatus() {
//        try {
//            List<MobilePayment> pendingPayments = mobilePaymentService.findByStatusAndApplication("PENDING", "InternetBanking");
//            for (MobilePayment payment : pendingPayments) {
//                processPaymentStatus(payment);
//            }
//        } catch (Exception e) {
//            // Handle exception, e.g., log it
//            e.printStackTrace();
//        }
//    }
//    private void processPaymentStatus(MobilePayment payment) throws Exception {
//        PaymentDto paymentDto = paymentService.transactionStatus(payment.getPaymentGatewaysUuid());
//        String status = paymentDto.getData().getTransaction().getStatus().toString().trim();
//        switch (status.trim()) {
//            case "PENDING" -> mobilePaymentService.updateMobilePayment(payment);
//            case "FAILED", "CANCELLED" -> handleFailedOrCancelledPayment(payment);
//            case "SUCCESS" -> handleSuccessfulPayment(payment);
//            default -> {
//            }
//            // Handle unexpected status if necessary
//        }
//    }
    private void handleFailedOrCancelledPayment(MobilePayment payment) throws Exception {
        if (payment.getType().trim().equals("WITHDRAWAL")) {
            AccountCallback accountCallback = new AccountCallback();
            accountCallback.setAccount(payment.getCpteJumelle());
            accountCallback.setTrxNumber(payment.getTrxNumber());
            accountCallback.setTelephone(payment.getTelephone());

            mobilePaymentService.account_callback(accountCallback);
        }
        payment.setCallBackReceive(true);
        mobilePaymentService.updateMobilePayment(payment);
    }

    private void handleSuccessfulPayment(MobilePayment payment) throws Exception {
        if (payment.getType().trim().equals("DEPOSIT")) {
            AccountMvtDto accountMvt = createAccountMvtDto(payment);
            AccountMvtDto result = mobilePaymentService.account_mvt(accountMvt);
            payment.setTrxNumber(result.getPc_OutID());

            if (result.getPc_OutLECT() != 0) {
                // Handle unsuccessful operation if needed
                return;
            }
        }
        payment.setCallBackReceive(true);
        mobilePaymentService.updateMobilePayment(payment);
    }

    private AccountMvtDto createAccountMvtDto(MobilePayment payment) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String currentDate = dateFormat.format(new Date());

        AccountMvtDto accountMvt = new AccountMvtDto();
        accountMvt.setAccountId(payment.getCpteJumelle());
        accountMvt.setPhoneNumber(payment.getTelephone());
        accountMvt.setAmount(payment.getMontant());
        accountMvt.setDate(currentDate);
        accountMvt.setIds("");
        accountMvt.setTypeOp(payment.getTypeOperation());
        accountMvt.setSens(1);
        accountMvt.setDescription(payment.getDescription());

        return accountMvt;
    }
    private void notification(ClientConfig config, String eventCode, List<Object> payload) {
        Optional<Subscriptions> sub = subscriptionRepository.findByClientMatricul(config.getClient());
        if (sub.isPresent()) {
            Optional<NotificationTemplate> notificationTemplate = notificationTemplateRepository.findByNotificationTypeAndEventCode(sub.get().getPreferedNotificationChanel(), eventCode);
            if (notificationTemplate.isPresent()) {
//            Subscriptions subs = config;

//            NotificationEvent event = new NotificationEvent();
//            event.setEventCode(EventCode.ACCOUNT_TRANSFER.name());
//            event.setPayload(payload);
//            event.setType(NotificationChanel.MAIL);
//            event.setDestination(config.getSubscriptions().getEmail());
//            event.setSubscriber(subs);

                String Message = replaceParameters(notificationTemplate.get().getTemplate(), payload);
                String Subject = replaceParameters(notificationTemplate.get().getSubject(), payload);

//            event.setEventCode(eventCode);
//            System.out.println("Received custom event:{" + event.getPayLoad() + "} " + event.getPayLoad().getTemplate());
                emailService.sendSimpleMessage(config.getSubscriptions().getEmail(), Subject, Message);
//        applicationEventPublisher.publishEvent(event);
            }
        }


    }

}
