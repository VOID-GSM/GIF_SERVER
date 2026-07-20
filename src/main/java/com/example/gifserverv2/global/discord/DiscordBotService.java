package com.example.gifserverv2.global.discord;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class DiscordBotService {

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private JDA jda;

    @Value("${app.discord.notice-channel-id:}")
    private String noticeChannelIds;

    public void sendNoticeMessage(String content) {
        if (jda == null) {
            log.warn("디스코드 봇이 초기화되지 않아 메시지를 보내지 않습니다.");
            return;
        }
        if (noticeChannelIds == null || noticeChannelIds.isBlank()) {
            log.warn("공지 채널 ID가 설정되지 않아 메시지를 보내지 않습니다.");
            return;
        }

        List<String> channelIds = Arrays.stream(noticeChannelIds.split(","))
                .map(String::trim)
                .filter(id -> !id.isBlank())
                .toList();

        for (String channelId : channelIds) {
            try {
                TextChannel channel = jda.getTextChannelById(channelId);
                if (channel == null) {
                    log.error("채널을 찾을 수 없습니다. channelId={}", channelId);
                    continue;
                }
                channel.sendMessage(content).queue();
            } catch (Exception e) {
                log.error("디스코드 메시지 전송 실패. channelId={}", channelId, e);
            }
        }
    }
}