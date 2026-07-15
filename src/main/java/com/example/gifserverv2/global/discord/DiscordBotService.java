package com.example.gifserverv2.global.discord;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordBotService {

    private final JDA jda;

    @Value("${app.discord.notice-channel-id:}")
    private String noticeChannelId;

    public void sendNoticeMessage(String content) {
        if (jda == null) {
            log.warn("디스코드 봇이 초기화되지 않아 메시지를 보내지 않습니다.");
            return;
        }
        if (noticeChannelId == null || noticeChannelId.isBlank()) {
            log.warn("공지 채널 ID가 설정되지 않아 메시지를 보내지 않습니다.");
            return;
        }

        try {
            TextChannel channel = jda.getTextChannelById(noticeChannelId);
            if (channel == null) {
                log.error("채널을 찾을 수 없습니다. channelId={}", noticeChannelId);
                return;
            }
            channel.sendMessage(content).queue();
        } catch (Exception e) {
            log.error("디스코드 메시지 전송 실패", e);
        }
    }
}