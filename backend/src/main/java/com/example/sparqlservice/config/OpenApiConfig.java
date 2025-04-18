package com.example.sparqlservice.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
class OpenApiConfig {

  private final String APP_VERSION;
  private final String STAGING_SERVER_URL;

  public OpenApiConfig(@Value("${app.version}") String appVersion, @Value("${open-api.server}") String stagingServerUrl) {
    this.APP_VERSION = appVersion;
    this.STAGING_SERVER_URL = stagingServerUrl;
  }

  @Bean
  OpenAPI defineOpenApi() {
    Info info = info();
    Server stagingServer = stagingServer();
    Server localhost = localhost();
    SecurityRequirement securityRequirement = securityRequirement();
    Components components = components();

    return new OpenAPI()
        .info(info)
        .servers(List.of(stagingServer, localhost))
        .addSecurityItem(securityRequirement)
        .components(components);
  }

  private Info info() {
    Info info = new Info()
        .title("Sparql Endpoint Server")
        .version(APP_VERSION);
    return info;
  }

  private Server stagingServer() {
    Server stagingServer = new Server();
    stagingServer.setUrl(STAGING_SERVER_URL);
    stagingServer.setDescription("ステージングサーバー");
    return stagingServer;
  }

  private Server localhost() {
    Server localhost = new Server();
    localhost.setUrl("http://localhost:8080");
    localhost.setDescription("ローカルホスト");
    return localhost;
  }

  private SecurityRequirement securityRequirement() {
    return new SecurityRequirement().addList("API Key Authentication");
  }

  private Components components() {
    return new Components().addSecuritySchemes("API Key Authentication",
        new SecurityScheme().type(SecurityScheme.Type.APIKEY)
            .in(SecurityScheme.In.HEADER)
            .name("Authorization")
            .description("API Key Authentication"));
  }

}