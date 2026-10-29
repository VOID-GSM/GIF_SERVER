package com.example.gifserverv2.domain.assessment.roadmaking.model;

import java.util.List;

public record Trace(List<Position> path, boolean reached) {
}
