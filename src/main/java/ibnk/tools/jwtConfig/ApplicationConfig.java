package ibnk.tools.jwtConfig;

import ibnk.tools.security.CustomAuthenticationProvider;
import ibnk.tools.security.SecuritySubscriptionService;
import ibnk.tools.security.SecurityUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;


@Configuration
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
@EnableWebSecurity
public class ApplicationConfig  {
    private final SecurityUserService userService;
    private final SecuritySubscriptionService clientService;
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource
                = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames(
                "classpath:/messages/api_error_messages"
        );
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }


    @Bean
    @Primary
    public SecurityUserService userDetailsService() {
        System.out.println("Enter UserDetailService Bean");
        return userService;
    }

    @Bean
    public AuthenticationProvider authenticationProviderClient() {
        return new CustomAuthenticationProvider.CustomAuthenticationClientProvider(clientDetailsService());
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        return new CustomAuthenticationProvider(userDetailsService(), passwordEncoder());
    }

    @Bean
    @Primary
    public SecuritySubscriptionService clientDetailsService() {
        System.out.println("Enter ClientDetailService Bean");
        return clientService;
    }



    @Bean
    public AuthenticationManager authenticationManager() {
        List<AuthenticationProvider> providers = Arrays.asList(authenticationProviderClient(), authenticationProvider());
        return new ProviderManager(providers);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Collections.singletonList("*"));
        config.setAllowedHeaders(Arrays.asList(
                HttpHeaders.ORIGIN,
                HttpHeaders.ACCEPT,
                HttpHeaders.CONTENT_TYPE,
                HttpHeaders.AUTHORIZATION,
                "X-User-Security",
                "X-Device-Info",
                "X-Device",
                "T",
                "X-Forwarded-For"
        ));
        config.setAllowedMethods(Arrays.asList(
                "GET",
                "POST",
                "DELETE",
                "PUT",
                "PATCH"
        ));
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Configuration
    @RequiredArgsConstructor
    public class ClientTokenSecurityConfigurationAdapter {
        private final JwtTokenFilter jwtAuthFilter;

        @Bean
        public UserDetailsService userDetailsService_User2() {
            return new SecuritySubscriptionService();
        }

        private final AuthenticationProvider authenticationProvider = new CustomAuthenticationProvider.CustomAuthenticationClientProvider(clientService);

        @Bean
        public SecurityFilterChain securityClientFilterChain(HttpSecurity http) throws Exception {
            http
                    .securityMatcher("/api/v1/client/**")
                    .authenticationProvider(authenticationProvider)
                    .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                    .cors()
                    .and()
                    .csrf()
                    .disable()
                    .authorizeHttpRequests()
                    .requestMatchers("/api/v1/otp/**", "/api/v1/client/auth/**", "http://localhost:8888/w1", "/v3/api-docs", "/v3/api-docs/swagger-config", "/swagger-resources", "/swagger-resources/**", "/configuration/ui", "/configuration/security", "/webjars/**", "/swagger-ui/**", "/swagger-ui.html")
                    .permitAll()
                    .anyRequest()
                    .authenticated()
                    .and()
                    .userDetailsService(new SecuritySubscriptionService())
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                    .exceptionHandling();
            http.httpBasic();
            return http.build();
        }

    }

    @Component
    public static class ReportFileHandler implements ApplicationListener<ContextRefreshedEvent> {
      private static final   String REPORT_RESOURCE_PATTERN = "classpath:/report/*.rpt";
        private static final String REPORTS_FOLDER_PATH = "C:/Reports";

        @Override
        public void onApplicationEvent(ContextRefreshedEvent event) {
            try {
                ensureReportsOnDisk();
            } catch (IOException e) {
                e.printStackTrace();
                // Handle the exception as needed
            }
        }
public static void ensureReportsOnDisk() throws IOException {
    Path reportsFolderPath = Paths.get(REPORTS_FOLDER_PATH);
    if (!Files.exists(reportsFolderPath)) {
        Files.createDirectories(reportsFolderPath); // Create reports folder if it doesn't exist
    }

    try {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(REPORT_RESOURCE_PATTERN);

        for (Resource resource : resources) {
            String filename = resource.getFilename();
            if (filename != null) {
                Path targetFile = reportsFolderPath.resolve(filename);
                try (InputStream inputStream = resource.getInputStream()) {
                    Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Report file copied to disk: " + targetFile);
                }
            }
        }
    } catch (IOException e) {
        throw new IOException("Error copying report files", e);
    }
}






    }



}
