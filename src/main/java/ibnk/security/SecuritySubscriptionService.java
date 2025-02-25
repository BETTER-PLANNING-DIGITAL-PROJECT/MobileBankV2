package ibnk.security;

import ibnk.dto.auth.CustomerVerification;
import ibnk.models.banking.Client;
import ibnk.models.internet.ClientVerification;
import ibnk.models.internet.InstitutionConfig;
import ibnk.models.internet.client.Subscriptions;
import ibnk.models.internet.enums.Status;
import ibnk.models.internet.enums.SubscriberStatus;
import ibnk.models.internet.enums.VerificationType;
import ibnk.repositories.banking.ClientMatriculRepository;
import ibnk.repositories.internet.ClientVerificationRepository;
import ibnk.repositories.internet.InstitutionConfigRepository;
import ibnk.repositories.internet.SubscriptionRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service()
@Transactional
@RequiredArgsConstructor
public class SecuritySubscriptionService implements UserDetailsService {
    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private ClientMatriculRepository clientMatriculRepository;

    @Autowired
    private ClientVerificationRepository clientVerificationRepository;

    @Autowired
    private  InstitutionConfigRepository institutionConfigRepository;

    private final Logger LOGGER = LogManager.getLogger(SecuritySubscriptionService.class);
    @Override
    public Subscriptions loadUserByUsername(String userLogin) throws UsernameNotFoundException {
        LOGGER.info("Enter >> loadClientByUsername");
        Subscriptions user = subscriptionRepository.findByUserLogin(userLogin).orElseThrow(() -> new UsernameNotFoundException("invalid username or password"));
        Client matricul = clientMatriculRepository.findById(user.getClientMatricul()).orElseThrow(() -> new UsernameNotFoundException("invalid username or password ."));
        user.setClient(matricul);
        LOGGER.info("Exit >> loadClientByUsername");
        return user;
    }

    public Optional<Subscriptions> loadClientByUuid(String uuid) {
        return subscriptionRepository.findByUuid(uuid);
    }


    public void saveAuditLog(Subscriptions client, String message, Status status)  {
        ClientVerification verify = ClientVerification.builder()
                .subscriptions(client)
                .status(status)
                .role("LOGIN")
                .verified(false)
                .verificationType(VerificationType.USER)
                .message(message)
                .build();
        clientVerificationRepository.save(verify);

        InstitutionConfig config = institutionConfigRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new ValidationException("Empty"));

        LocalDateTime time = LocalDateTime.now().minusMinutes(config.getVerificationResetTimer());
        Integer previousTrials = clientVerificationRepository.countPreviousFailedTrials(client, Status.FAILED, time,  VerificationType.USER);
        Integer leftTrials = Math.toIntExact(config.getMaxVerifyAttempt() - previousTrials);
        CustomerVerification responseData = new CustomerVerification();

        responseData.setTrials(leftTrials);
        responseData.setMaxTrials(Math.toIntExact(config.getMaxVerifyAttempt()));
        if (leftTrials.compareTo(1) < 0) {
            client.setStatus(SubscriberStatus.SUSPENDED.name());
            subscriptionRepository.save(client);
        };
    }
}
