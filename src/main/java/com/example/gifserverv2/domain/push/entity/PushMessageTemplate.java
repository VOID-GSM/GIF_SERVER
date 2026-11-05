package com.example.gifserverv2.domain.push.entity;

import com.example.gifserverv2.domain.push.service.PushOptions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nl.martijndwars.webpush.Urgency;

import java.time.Duration;

@Getter
@RequiredArgsConstructor
public enum PushMessageTemplate {

    PROJECT_CREATED("🆕 새로운 프로젝트가 생성되었습니다.", "'%s' 프로젝트가 신규 등록되었습니다.", "/projects/%d"),

    DEADLINE_3_DAYS("📢 [D-3] 양식 마감 안내", "'%s' 양식 마감까지 3일 남았습니다.", "/form/%d/submit"),
    DEADLINE_1_DAY("⚠️ [D-1] 양식 마감 임박", "'%s' 양식 마감까지 1일 남았습니다. 서둘러 제출해 주세요.", "/form/%d/submit"),
    DEADLINE_5_HOURS("🚨 [긴급] 양식 마감 5시간 전!", "'%s' 양식 마감까지 5시간 남았습니다. 작성 상태를 확인해 주세요.", "/form/%d/submit"),
    DEADLINE_1_HOUR("🔥 [초긴급] 양식 마감 1시간 전!", "'%s' 양식 마감이 1시간 남았습니다! 지금 바로 제출해 주세요.", "/form/%d/submit"),
    DEADLINE_MISSED("❌ [마감 경과] 양식 미제출 안내", "'%s' 양식 제출 기한이 지났습니다. 아직 제출하지 않은 팀은 조속히 제출 바랍니다.", "/form/%d/submit"),

    INQUIRY_ANSWERED("💬 문의하신 글에 답변이 등록되었습니다.", "작성하신 문의사항에 대해 관리자의 답변이 완료되었습니다.", "/inquiry/my/%d"),
    INQUIRY_RECEIVED("📩 새로운 문의사항이 접수되었습니다.", "새로운 문의글이 등록되었습니다. 확인 후 답변해 주세요.", "/inquiry/admin/%d"),

    TEAM_MEMBER_ADDED("🤝 새로운 프로젝트에 팀원으로 추가되었습니다!", "'%s' 프로젝트에 팀원으로 등록되었습니다. 확인해 보세요.", "/projects/%d"),
    TEAM_MEMBER_REMOVED("📌 프로젝트 팀원에서 제외되었습니다.", "'%s' 프로젝트 팀원 목록에서 제외되었습니다.", "/"),

    FORM_SUBMITTED("📥 [제출 알림] 새로운 양식이 제출되었습니다.", "유저가 '%s' 양식을 제출했습니다. 확인해 보세요.", "/form/submissions/%d"),
    FORM_ANNOUNCED("📢 새로운 양식이 등록되었습니다.", "'%s' 양식이 등록되었습니다. 기간 내에 작성해 주세요.", "/form/%d/submit"),

    SCORE_DEADLINE_WARNING("⚠️ [점수 마감 임박]", "전체 프로젝트 점수 산정 마감까지 %d시간 남았습니다. 점수를 입력해 주세요.", "/score"),
    SCORE_PUBLISHED("📊 [점수 공지]", "전체 프로젝트 점수 및 등수 공지가 완료되었습니다. 지금 확인해 보세요!", "/rank"),

    LEADER_TRANSFERRED_CLIENT("👑 팀장 변경 알림", "'%s' 프로젝트의 팀장이 '%s' 님으로 변경되었습니다.", "/projects/%d"),
    LEADER_TRANSFERRED_ADMIN("👑 [프로젝트 관리] 팀장 변경 알림", "%s 선생님에 의해 '%s' 프로젝트의 팀장이 '%s' 님으로 변경되었습니다.", "/projects/%d"),

    SCHEDULE_CURRENT_PERIOD("📅 [현재 일정 안내]", "'%s' 프로젝트의 '%s' 기간(%s ~ %s)입니다. 일정에 맞춰 진행해 주세요!", "/projects/%d");

    private final String title;
    private final String bodyTemplate;
    private final String targetUrlTemplate;

    /**
     * 마감 알림은 다음 단계 알림 전까지만 의미가 있으므로 TTL 을 짧게 두어,
     * 기기가 오프라인일 때 지난 알림이 나중에 몰아서 도착하지 않게 한다. (#208)
     */
    public PushOptions getPushOptions() {
        return switch (this) {
            case DEADLINE_3_DAYS -> new PushOptions(Duration.ofHours(48), Urgency.HIGH);  // D-1 알림 전까지
            case DEADLINE_1_DAY -> new PushOptions(Duration.ofHours(19), Urgency.HIGH);   // 5시간 전 알림 전까지
            case DEADLINE_5_HOURS -> new PushOptions(Duration.ofHours(4), Urgency.HIGH);  // 1시간 전 알림 전까지
            case DEADLINE_1_HOUR -> new PushOptions(Duration.ofHours(1), Urgency.HIGH);   // 마감 시각까지
            default -> PushOptions.DEFAULT;
        };
    }

    public String formatBody(Object... args) {
        if (args == null || args.length == 0) {
            return this.bodyTemplate;
        }
        return String.format(this.bodyTemplate, args);
    }

    public String formatTargetUrl(Object... args) {
        if (targetUrlTemplate == null) return null;
        if (args == null || args.length == 0) {
            return this.targetUrlTemplate;
        }
        return String.format(this.targetUrlTemplate, args);
    }
}