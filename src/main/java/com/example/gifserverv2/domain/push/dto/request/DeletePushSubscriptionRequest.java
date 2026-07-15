package com.example.gifserverv2.domain.push.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DeletePushSubscriptionRequest {

    @NotBlank(message = "endpoint는 필수값입니다.")
    private String endpoint;
}
