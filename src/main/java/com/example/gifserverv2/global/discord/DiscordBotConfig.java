package com.example.gifserverv2.global.discord;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Slf4j
@Configuration
public class DiscordBotConfig {

    @Value("${app.discord.bot-token:}")
    private String botToken;

    @Bean
    public JDA jda() throws InterruptedException {
        if (botToken == null || botToken.isBlank()) {
            log.warn("디스코드 봇 토큰이 설정되지 않아 봇을 실행하지 않습니다.");
            return null;
        }

        JDA jda = JDABuilder.createLight(botToken, Collections.emptyList())
                .build();
        jda.awaitReady();
        log.info("디스코드 봇 로그인 완료");
        return jda;
    }
}