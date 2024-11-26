package de.dhbw_ravensburg.webeng2.backend.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SpringDocConfig {

   @Bean
   public OpenAPI api() {
        return new OpenAPI()
        .info(
            new Info()
            .title("OpenReadMap Backend API")
            .description("API Documentation for OpenReadMap")
            .contact(new Contact().email("contact@openreadmap.de").name("Team OpenReadMap"))
            .license(new License().name("GPLv3"))
        );
   } 
}
