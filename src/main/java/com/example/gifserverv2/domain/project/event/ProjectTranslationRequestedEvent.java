package com.example.gifserverv2.domain.project.event;

// 프로젝트 소개의 일본어 번역이 필요할 때 발행한다. (생성·소개 수정, 번역이 없는 상태로 일본어 조회)
public record ProjectTranslationRequestedEvent(Long projectId) {}
