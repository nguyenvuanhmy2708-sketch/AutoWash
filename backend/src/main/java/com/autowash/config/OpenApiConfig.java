package com.autowash.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.media.Schema; // Thêm import này
import org.springdoc.core.utils.SpringDocUtils; // Thêm import này
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class OpenApiConfig {

    static {
        // TỰ ĐỘNG: Cứ chỗ nào là LocalDate, Swagger sẽ tự động render thành chuỗi ngày hôm nay thực tế
        Schema<LocalDate> localDateSchema = new Schema<>();
        localDateSchema.setType("string");
        localDateSchema.setFormat("date");
        localDateSchema.example(LocalDate.now().toString());

        SpringDocUtils.getConfig().replaceWithSchema(LocalDate.class, localDateSchema);
    }

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
        new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}