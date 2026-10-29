package com.example.gifserverv2.domain.assessment.roadmaking.model;

import java.util.List;

public record Board(int rows, int cols, List<Vehicle> vehicles, List<Person> people) {

    public boolean inBounds(Position p) {
        return p.row() >= 0 && p.row() < rows && p.col() >= 0 && p.col() < cols;
    }

    public boolean isOccupied(Position p) {
        return vehicles.stream().anyMatch(v -> v.position().equals(p))
                || people.stream().anyMatch(person -> person.position().equals(p));
    }
}