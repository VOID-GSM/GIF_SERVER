package com.example.gifserverv2.domain.assessment.catchase.entity;

public enum Judgment {
    FOUND,
    MISSED,
    // 응답 제한 시간 초과. 쥐가 있었는지와 관계없이 오답이다
    TIMEOUT
}
