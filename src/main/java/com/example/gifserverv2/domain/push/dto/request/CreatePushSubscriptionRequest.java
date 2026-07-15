package com.example.gifserverv2.domain.push.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreatePushSubscriptionRequest {

    @NotBlank(message = "endpoint는 필수값입니다.")
    private String endpoint;

    @NotBlank(message = "p256dh는 필수값입니다.")
    private String p256dh;

    @NotBlank(message = "auth는 필수값입니다.")
    private String auth;
}
