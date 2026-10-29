package com.example.gifserverv2.domain.assessment.roadmaking.service;

import com.example.gifserverv2.domain.assessment.roadmaking.exception.RoadMakingErrorCode;
import com.example.gifserverv2.domain.assessment.roadmaking.exception.RoadMakingException;
import com.example.gifserverv2.domain.assessment.roadmaking.model.Board;
import com.example.gifserverv2.domain.assessment.roadmaking.model.Color;
import com.example.gifserverv2.domain.assessment.roadmaking.model.Direction;
import com.example.gifserverv2.domain.assessment.roadmaking.model.Person;
import com.example.gifserverv2.domain.assessment.roadmaking.model.Position;
import com.example.gifserverv2.domain.assessment.roadmaking.model.Vehicle;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class ProblemGenerator {
    private static final int PROBLEMS_PER_LEVEL = 2;
    private static final int MAX_ATTEMPTS = 2000;

    public record Difficulty(int rows, int cols, int pairs, int minFence, int maxFence) {
    }

    public record GeneratedProblem(Board board, int minFenceCount) {
    }

    private static final int BOARD_SIZE = 6;

    private static final List<Difficulty> DIFFICULTIES = List.of(
            new Difficulty(BOARD_SIZE, BOARD_SIZE, 3, 1, 2),
            new Difficulty(BOARD_SIZE, BOARD_SIZE, 3, 2, 3),
            new Difficulty(BOARD_SIZE, BOARD_SIZE, 3, 3, 4),
            new Difficulty(BOARD_SIZE, BOARD_SIZE, 4, 3, 4),
            new Difficulty(BOARD_SIZE, BOARD_SIZE, 4, 4, 5)
    );

    public GeneratedProblem generate(int solvedCount) {
        int level = Math.min(solvedCount / PROBLEMS_PER_LEVEL, DIFFICULTIES.size() - 1);

        for (int l = level; l >= 0; l--) {
            Difficulty difficulty = DIFFICULTIES.get(l);
            for (int i = 0; i < MAX_ATTEMPTS; i++) {
                GeneratedProblem problem = tryGenerate(difficulty);
                if (problem != null) {
                    return problem;
                }
            }
        }
        throw new RoadMakingException(RoadMakingErrorCode.PROBLEM_GENERATION_FAILED);
    }

    private GeneratedProblem tryGenerate(Difficulty d) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        List<Position> cells = new ArrayList<>();
        for (int r = 0; r < d.rows(); r++) {
            for (int c = 0; c < d.cols(); c++) {
                cells.add(new Position(r, c));
            }
        }
        Collections.shuffle(cells, rnd);

        List<Color> colors = new ArrayList<>(List.of(Color.values()));
        Collections.shuffle(colors, rnd);

        Direction[] directions = Direction.values();
        List<Vehicle> vehicles = new ArrayList<>();
        for (int i = 0; i < d.pairs(); i++) {
            vehicles.add(new Vehicle(colors.get(i), cells.get(i), directions[rnd.nextInt(directions.length)]));
        }

        int fenceCount = rnd.nextInt(d.minFence(), d.maxFence() + 1);
        boolean[][] fence = new boolean[d.rows()][d.cols()];
        for (int i = 0; i < fenceCount; i++) {
            Position p = cells.get(d.pairs() + i);
            fence[p.row()][p.col()] = true;
        }

        PathSimulator withoutPeople = new PathSimulator(new Board(d.rows(), d.cols(), vehicles, List.of()));
        Set<Position> occupied = new HashSet<>();
        vehicles.forEach(v -> occupied.add((Position) v.position()));

        List<Person> people = new ArrayList<>();
        for (Vehicle vehicle : vehicles) {
            List<Position> candidates = withoutPeople.trace(vehicle, fence).path().stream()
                    .skip(1)
                    .filter(p -> !occupied.contains(p))
                    .distinct()
                    .toList();
            if (candidates.isEmpty()) {
                return null;
            }
            Position position = candidates.get(rnd.nextInt(candidates.size()));
            occupied.add(position);
            people.add(new Person(vehicle.color(), position));
        }

        Board board = new Board(d.rows(), d.cols(), vehicles, people);

        PathSimulator simulator = new PathSimulator(board);
        if (!simulator.isSolved(fence)) {
            return null;
        }

        int min = findMinFences(board, simulator, fenceCount);
        if (min < d.minFence()) {
            return null;
        }
        return new GeneratedProblem(board, min);
    }

    private int findMinFences(Board board, PathSimulator simulator, int k) {
        List<Position> candidates = new ArrayList<>();
        for (int r = 0; r < board.rows(); r++) {
            for (int c = 0; c < board.cols(); c++) {
                Position p = new Position(r, c);
                if (!board.isOccupied(p)) {
                    candidates.add(p);
                }
            }
        }
        boolean[][] fence = new boolean[board.rows()][board.cols()];
        for (int count = 0; count < k; count++) {
            if (search(candidates, 0, count, fence, simulator)) {
                return count;
            }
        }
        return k;
    }

    private boolean search(List<Position> candidates, int start, int remaining,
                           boolean[][] fence, PathSimulator simulator) {
        if (remaining == 0) {
            return simulator.isSolved(fence);
        }
        for (int i = start; i <= candidates.size() - remaining; i++) {
            Position p = candidates.get(i);
            fence[p.row()][p.col()] = true;
            boolean found = search(candidates, i + 1, remaining - 1, fence, simulator);
            fence[p.row()][p.col()] = false;
            if (found) {
                return true;
            }
        }
        return false;
    }
}