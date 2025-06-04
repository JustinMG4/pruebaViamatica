package com.justindev.prueba_telconet.application.config.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.http.HttpHeaders;

@OpenAPIDefinition(
        info = @Info(
                title = "Prueba Telconet",
                version = "1.0.0",
                description = "API REST para la prueba de Telconet de Justin Moreira",
                contact = @Contact(
                        name = "Justin Moreira",
                        email = "justinmoreiragarcia@gmail.com"
                ),
                license = @io.swagger.v3.oas.annotations.info.License(
                        name = "Apache 2.0",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"
                )
        ),
        servers = {
                @Server(
                        description = "development",
                        url = "http://localhost:8080"
                )
        },
        security = @SecurityRequirement(
                name = "Security Token"
        )

)
@SecurityScheme(
        name = "Security Token",
        description = "Access token for the API",
        type = SecuritySchemeType.HTTP,
        paramName = HttpHeaders.AUTHORIZATION,
        in = SecuritySchemeIn.HEADER,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class SwaggerConfig {
}
