package ibnk.tools.security;

import ibnk.models.banking.Client;
import ibnk.models.internet.UserEntity;
import ibnk.models.internet.client.Subscriptions;
import ibnk.models.internet.enums.Status;
import ibnk.models.internet.enums.SubscriberStatus;
import ibnk.repositories.banking.ClientMatriculRepository;
import ibnk.tools.error.FailedLoginException;
import ibnk.tools.error.UnauthorizedUserException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
    private final SecurityUserService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public CustomAuthenticationProvider(SecurityUserService userDetailsService, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        UserEntity user = this.userDetailsService.loadUserByUsername(username);

        String userUuid = null;
        String loginType = null;

        if(user != null) {
            userUuid = user.getUuid();
            loginType = "ADMIN";
        }
        assert user != null;
        if (!this.passwordEncoder.matches(password, user.getPassword())) {
            throw new FailedLoginException("failed_login",userUuid, "FAILED_PASSWORD", loginType);
        }
        return new UsernamePasswordAuthenticationToken(user, password, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

    @Component
    public static class CustomAuthenticationClientProvider implements AuthenticationProvider {
        private final SecuritySubscriptionService securitySubscriptionService;



        public CustomAuthenticationClientProvider(SecuritySubscriptionService securitySubscriptionService) {
            this.securitySubscriptionService = securitySubscriptionService;
                 }


        @Override
        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
            String username = authentication.getName();
            String password = authentication.getCredentials().toString();

            Subscriptions client = this.securitySubscriptionService.loadUserByUsername(username);
            assert client != null;

            return new UsernamePasswordAuthenticationToken(client, password, client.getAuthorities());
        }

        @Override
        public boolean supports(Class<?> authentication) {
            return authentication.equals(UsernamePasswordAuthenticationToken.class);
        }
    }
}
