package com.ele.javademo.configs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Value("${server.port}")
  private String serverPort;

  @Bean
  public OpenAPI customOpenAPI() {
    Server localServer = new Server();
    localServer.setUrl("http://localhost:" + serverPort);
    localServer.setDescription("Local Development Server");

    Contact contact = new Contact();
    contact.setName("Ele Team");
    contact.setEmail("support@ele.com");

    Info info =
        new Info()
            .title("Ele Java Demo API")
            .version("1.0.0")
            .description("API documentation for User Management System")
            .contact(contact)
            .license(
                new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html"));

    return new OpenAPI().info(info).servers(List.of(localServer));
  }
}
