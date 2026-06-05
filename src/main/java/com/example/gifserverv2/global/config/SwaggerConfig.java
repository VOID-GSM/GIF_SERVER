package com.example.gifserverv2.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;

public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("gif_v2 API")
                        .description("gif_v2 API 문서")
                        .version("1.0.0"));
    }
}
