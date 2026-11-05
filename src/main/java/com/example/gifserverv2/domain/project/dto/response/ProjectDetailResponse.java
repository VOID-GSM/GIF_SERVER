package com.example.gifserverv2.domain.project.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(oneOf = {DetailProjectResponse.class, PublicDetailProjectResponse.class})
public sealed interface ProjectDetailResponse
        permits DetailProjectResponse, PublicDetailProjectResponse {
}