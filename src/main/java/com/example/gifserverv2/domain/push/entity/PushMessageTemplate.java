package com.example.gifserverv2.domain.push.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PushMessageTemplate {

    DEADLINE_URGENT("🚨 [초긴급] 양식 마감 임박!", "'%s' 양식 마감이 %d시간 남았습니다! 지금 바로 제출해 주세요."),
    DEADLINE_WARNING("⚠️ [긴급] 양식 마감 D-1", "'%s' 양식 마감일이 얼마 남지 않았습니다. 서둘러 작성해 주세요."),
    DEADLINE_REMINDER("📢 양식 마감 안내", "'%s' 양식 마감까지 %d시간 남았습니다."),

    INQUIRY_ANSWERED("💬 문의하신 글에 답변이 등록되었습니다.", "작성하신 문의사항의에 대해 관리자의 답변이 완료되었습니다."),
    TEAM_MEMBER_ADDED("🤝 새로운 프로젝트에 팀원으로 추가되었습니다!", "'%s' 프로젝트에 팀원으로 등록되었습니다. 확인해 보세요."),

    FORM_SUBMITTED("📥 [제출 알림] 새로운 양식이 제출되었습니다.", "유저가 '%s' 양식을 제출했습니다. 확인해 보세요."),
    SCORE_DEADLINE_WARNING("⚠️ [점수 마감 임박]", "전체 프로젝트 점수 산정 마감까지 %d시간 남았습니다. 점수를 입력해 주세요."),

    SCORE_PUBLISHED("📊 [점수 공지]", "전체 프로젝트 점수 및 등수 공지가 완료되었습니다. 지금 확인해 보세요!");

    private final String title;
    private final String bodyTemplate;

    public String formatBody(Object... args) {
        if (args == null || args.length == 0) {
            return this.bodyTemplate;
        }
        return String.format(this.bodyTemplate, args);
    }

    public String getBody() {
        return this.bodyTemplate;
    }
}