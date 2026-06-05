package com.example.gifserverv2.domain.auth.dto.request;

import com.example.gifserverv2.domain.user.entity.ClientRole;
import jakarta.validation.constraints.NotNull;

public record ClientAdditionalInfoRequest(
        @NotNull ClientRole clientRole) {
}
