package com.example.gifserverv2.domain.retrospective.entity;

import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RetrospectiveRelatedWork {

    @Enumerated(EnumType.STRING)
    @Column(name = "work_type", length = 10)
    private RelatedWorkType type;

    @Column(name = "work_number")
    private Integer number;

    @Column(name = "work_title", length = 200)
    private String title;
}
