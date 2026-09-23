package com.example.gifserverv2.domain.assessment.roadmaking.service;

import com.example.gifserverv2.domain.assessment.roadmaking.model.Board;
import com.example.gifserverv2.domain.assessment.roadmaking.model.Color;
import com.example.gifserverv2.domain.assessment.roadmaking.model.Direction;
import com.example.gifserverv2.domain.assessment.roadmaking.model.Person;
import com.example.gifserverv2.domain.assessment.roadmaking.model.Position;
import com.example.gifserverv2.domain.assessment.roadmaking.model.Trace;
import com.example.gifserverv2.domain.assessment.roadmaking.model.Vehicle;

import java.util.ArrayList;
import java.util.List;

public class PathSimulator {
    private final int rows;
    private final int cols;
    private final Color[][] personGrid;
    private final List<Vehicle> vehicles;

    public PathSimulator(Board board) {
        this.rows = board.rows();
        this.cols = board.cols();
        this.vehicles = board.vehicles();
        this.personGrid = new Color[rows][cols];
        for (Person person : board.people()) {
            personGrid[person.position().row()][person.position().col()] = person.color();
        }
    }

    public boolean isSolved(boolean[][] fence) {
        for (Vehicle vehicle : vehicles) {
            if (!reaches(vehicle, fence)) {
                return false;
            }
        }
        return true;
    }

    public boolean reaches(Vehicle vehicle, boolean[][] fence) {
        return trace(vehicle, fence).reached();
    }

    public Trace trace(Vehicle vehicle, boolean[][] fence) {
        int r = vehicle.position().row();
        int c = vehicle.position().col();
        Direction dir = vehicle.direction();
        int limit = stepLimit();

        List<Position> path = new ArrayList<>();
        path.add(new Position(r, c));

        for (int step = 0; step < limit; step++) {
            int nr = r + dir.dRow();
            int nc = c + dir.dCol();
            if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) {
                return new Trace(path, false);
            }
            if (fence[nr][nc]) {
                dir = dir.rotate();
                continue;
            }
            r = nr;
            c = nc;
            path.add(new Position(r, c));
            Color person = personGrid[r][c];
            if (person != null) {
                return new Trace(path, person == vehicle.color());
            }
        }
        return new Trace(path, false);
    }

    private int stepLimit() {
        return rows * cols * 4;
    }
}