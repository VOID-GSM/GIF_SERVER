package com.example.gifserverv2.domain.user.dto.response;

import com.example.gifserverv2.domain.user.entity.UserLink;

public record UserLinkResponse(Long id, String title, String url) {

    public static UserLinkResponse from(UserLink link) {
        return new UserLinkResponse(link.getId(), link.getTitle(), link.getUrl());
    }
}
