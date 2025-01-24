package ibnk.tools.jwtConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import ibnk.dto.ClientDeviceDto;
import ibnk.models.banking.Client;
import ibnk.models.internet.InstitutionConfig;
import ibnk.models.internet.UserEntity;
import ibnk.models.internet.client.ClientDevice;
import ibnk.models.internet.client.ClientDeviceId;
import ibnk.models.internet.client.Subscriptions;
import ibnk.models.internet.enums.Application;
import ibnk.models.internet.enums.SubscriberStatus;
import ibnk.repositories.banking.ClientMatriculRepository;
import ibnk.repositories.internet.ClientDeviceRepository;
import ibnk.service.InstitutionConfigService;
import ibnk.tools.error.UnauthorizedUserException;
import ibnk.tools.security.SecuritySubscriptionService;
import ibnk.tools.security.SecurityUserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.Optional;


@Component
@RequiredArgsConstructor
public class JwtTokenFilter  extends OncePerRequestFilter  {
    private final JwtService jwtService;
    private final SecurityUserService userService;
    private final SecuritySubscriptionService subscriptionService;
    private final ClientMatriculRepository clientMatriculRepository;
    private final ClientDeviceRepository clientDeviceRepository;
    private final InstitutionConfigService institutionConfigService;

    @SneakyThrows
    @Override
    protected void doFilterInternal(
             HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)  {

        try {
            String authHeader = request.getHeader("Authorization");
//            String deviceId = getCookieValue(request, "user-device-cookie");

            String jwt;

            if(authHeader == null || !authHeader.startsWith("Bearer ")) {
                jwt = extractTokenFromQueryParameter(request);
            } else {
                jwt = authHeader.substring(7);
            }

            if (jwt == null || jwt.isEmpty()) {
                filterChain.doFilter(request, response);
                return;
            }

            String tokenType = jwtService.getTokenType(jwt);


            if ("User".equals(tokenType)) {
                    String userID = jwtService.extractUserID(jwt);
                    if (userID == null ) throw new UnauthorizedUserException("unauthorized_credentials");
                    UserEntity userDetails = userService.loadUserByUuid(userID);
                    if (!jwtService.isTokenValid(jwt, userDetails)) throw new UnauthorizedUserException("unauthorized_credentials");
                    authenticateUser(userDetails, request);
            }
            else if ("Client".equals(tokenType)) {
            String clientID = jwtService.extractClientID(jwt);
            if (clientID == null ) throw new UnauthorizedUserException("unauthorized_credentials");
            Optional<Subscriptions> userDetails = subscriptionService.loadClientByUuid(clientID);

                if(userDetails.isEmpty()) throw new UnauthorizedUserException("unauthorized_credentials");

                String deviceHeader = request.getHeader("X");
                if(deviceHeader.isEmpty()) {
                    throw new UnauthorizedUserException("");
                }
                InstitutionConfig config = institutionConfigService.findByyApp(Application.MB.name());

                if(config.isVerifyDevice()) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    ClientDeviceDto deviceInfo = objectMapper.readValue(deviceHeader, ClientDeviceDto.class);

                    ClientDeviceId deviceId = new ClientDeviceId(userDetails.get(), deviceInfo.getDeviceId(), deviceInfo.getDeviceToken());
                    Optional<ClientDevice> clientDevice = clientDeviceRepository.findById(deviceId);
                    if(clientDevice.isEmpty()) {
                        throw new UnauthorizedUserException("");
                    }

                    if(!clientDevice.get().getIsActive() || !clientDevice.get().getIsTrusted()) {
                        throw new UnauthorizedUserException("");
                    }
                }

            Client matricul = clientMatriculRepository.findById(userDetails.get().getClientMatricul()).orElseThrow(() -> new UsernameNotFoundException("failed_login"));
            userDetails.get().setClient(matricul);

            if(userDetails.get().getStatus().equals(SubscriberStatus.SUSPENDED.name())) throw new UnauthorizedUserException("account_suspended");
            if(userDetails.get().getStatus().equals(SubscriberStatus.BLOCKED.name())) throw new UnauthorizedUserException("account_blocked");
            if(userDetails.get().getStatus().equals(SubscriberStatus.PENDING.name())) throw new UnauthorizedUserException("failed_login");
            if(!userDetails.get().getFirstLogin()) throw new UnauthorizedUserException("failed_login");
            if(!userDetails.get().getContactVerification()) throw new UnauthorizedUserException("invalid_subscription");
            if (!jwtService.isTokenValidForClient(jwt, userDetails.get()))  throw new UnauthorizedUserException("unauthorized_credentials");
            authenticateUser(userDetails.get(), request);
            }
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
        }

    }

    private void authenticateUser(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    private String getCookieValue(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private String extractTokenFromQueryParameter(HttpServletRequest request) {
        return request.getParameter("token");
    }

}
