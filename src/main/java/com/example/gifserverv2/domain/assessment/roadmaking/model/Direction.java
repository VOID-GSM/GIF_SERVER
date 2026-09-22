package com.example.gifserverv2.domain.assessment.roadmaking.model;

public enum Direction {
    UP(-1, 0),
    RIGHT(0, 1),
    DOWN(1, 0),
    LEFT(0, -1);

    private static final Direction[] VALUES = values();

    private final int dRow;
    private final int dCol;

    Direction(int dRow, int dCol) {
        this.dRow = dRow;
        this.dCol = dCol;
    }

    public int dRow() {
        return dRow;
    }

    public int dCol() {
        return dCol;
    }

    public Direction rotate() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }
}