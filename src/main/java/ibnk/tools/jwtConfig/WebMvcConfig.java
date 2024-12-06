package ibnk.tools.jwtConfig;

import ibnk.tools.Interceptors.DeviceInterceptor;
import ibnk.tools.Interceptors.PinInterceptor;
import ibnk.tools.Interceptors.SecurityQuestionInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {
    private final SecurityQuestionInterceptor securityQuestionInterceptor;
    private final PinInterceptor pinInterceptor;
    private final DeviceInterceptor deviceInterceptor;

    @Autowired
    public WebMvcConfig(SecurityQuestionInterceptor securityQuestionInterceptor,PinInterceptor pinInterceptor, DeviceInterceptor deviceInterceptor) {
        this.securityQuestionInterceptor = securityQuestionInterceptor;
        this.pinInterceptor = pinInterceptor;
        this.deviceInterceptor = deviceInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(securityQuestionInterceptor);
        registry.addInterceptor(pinInterceptor);
        registry.addInterceptor(deviceInterceptor);
    }
}
