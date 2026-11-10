package com.example.gifserverv2.domain.push.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "웹 푸시 구독 등록 요청 (PushSubscription.toJSON() 값)")
@Getter
@NoArgsConstructor
public class CreatePushSubscriptionRequest {

    @Schema(description = "푸시 서비스 endpoint URL", example = "https://fcm.googleapis.com/fcm/send/abc...")
    @NotBlank(message = "endpoint는 필수값입니다.")
    private String endpoint;

    @Schema(description = "keys.p256dh 값", example = "BNcRdreALRFXTkOOUHK1EtK2wtaz5Ry4YfYCA_0QTpQtUbVlUls0VJXg7A8u-Ts1XbjhazAkj7I99e8QcYP7DkM")
    @NotBlank(message = "p256dh는 필수값입니다.")
    private String p256dh;

    @Schema(description = "keys.auth 값", example = "tBHItJI5svbpez7KI4CCXg")
    @NotBlank(message = "auth는 필수값입니다.")
    private String auth;
}
