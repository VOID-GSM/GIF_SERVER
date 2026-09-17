package com.example.gifserverv2.domain.retrospective.entity;

public enum RetrospectiveCategory {
    FEATURE_DEV("기능 개발"),
    FEATURE_IMPROVEMENT("기능 개선"),
    REFACTORING("리팩토링"),
    TROUBLESHOOTING("트러블슈팅"),
    RETROSPECTIVE("회고"),
    PORTFOLIO("포트폴리오");

    private final String description;

    RetrospectiveCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
