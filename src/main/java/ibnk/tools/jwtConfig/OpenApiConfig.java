package ibnk.tools.jwtConfig;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(info = @Info(contact = @Contact(name = "FontahPhilDev", email = "philfontah911@gmail.com", url = ""), description = "OpenApi documentation for GBLOAN", title = "OpenApi specification", version = "0.1", license = @License(name = "Licence name", url = ""), termsOfService = "Terms of service"), servers = {
        @Server(description = "Local ENV", url = ""), @Server(description = "PROD ENV", url = "") })

public class OpenApiConfig {
}
