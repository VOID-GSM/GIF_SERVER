package com.example.gifserverv2.domain.push.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "웹 푸시 구독 해제 요청")
@Getter
@NoArgsConstructor
public class DeletePushSubscriptionRequest {

    @Schema(description = "해제할 구독의 endpoint URL", example = "https://fcm.googleapis.com/fcm/send/abc...")
    @NotBlank(message = "endpoint는 필수값입니다.")
    private String endpoint;
}
