package com.nthalk.stacks;

import java.util.*;

public class Board {

    private static final char[] COLUMNS = "ABCDEFGHIJK".toCharArray();
    private final Set<Position> positions = new HashSet<>();
    Row[] rows = new Row[0];

    public Board() {
        makeRow(2, 10);
        makeRow(1, 10);
        makeRow(0, 10);
        makeRow(0, 9);
        makeRow(0, 8);

        for (Row row : rows) {
            Collections.addAll(positions, row.getPositions());
        }
    }

    private void makeRow(int startColumn, int endColumn) {
        rows = Arrays.copyOf(rows, rows.length + 1);
        rows[rows.length - 1] = new Row(rows.length - 1, startColumn, endColumn);

    }

    public Set<Position> getPositions() {
        return positions;
    }

    public Row[] getRows() {
        return rows;
    }

    public Set<Position> adjacentPositions(Position position) {
        Row row = position.getRow();
        Set<Position> adjacent = new HashSet<>();
        Position left = row.getPosition(position.getColumn() - 1);
        if (left != null) {
            adjacent.add(left);
        }
        Position right = row.getPosition(position.getColumn() + 1);
        if (right != null) {
            adjacent.add(right);
        }
        if (row.getNumber() > 0) {
            // We can go down
            Row rowBelow = getRows()[row.getNumber() - 1];
            Position downLeft = rowBelow.getPosition(position.getColumn());
            if (downLeft != null) {
                adjacent.add(downLeft);
            }
            Position downRight = rowBelow.getPosition(position.getColumn() + 1);
            if (downRight != null) {
                adjacent.add(downRight);
            }
        }

        if (row.getNumber() < rows.length - 1) {
            // We can go up
            Row rowAbove = getRows()[row.getNumber() + 1];
            Position upLeft = rowAbove.getPosition(position.getColumn());
            if (upLeft != null) {
                adjacent.add(upLeft);
            }
            Position upRight = rowAbove.getPosition(position.getColumn() - 1);
            if (upRight != null) {
                adjacent.add(upRight);
            }
        }
        return adjacent;
    }

    public static class Position {
        private final Row row;
        private final int column;

        private Position(Row row, int column) {
            this.row = row;
            this.column = column;
        }

        @Override
        public String toString() {
            return "" + COLUMNS[column] + (row.getNumber() + 1);
        }

        public Row getRow() {
            return row;
        }

        public int getColumn() {
            return column;
        }
    }

    public static class Stack {

        private final List<Game.Color> colors;


        public Stack(Stack place, Stack upon) {
            List<Game.Color> colors = new ArrayList<>();
            colors.addAll(upon.getColors());
            colors.addAll(place.getColors());
            this.colors = Collections.unmodifiableList(colors);
        }

        public Stack(Game.Color currentColor) {
            this.colors = Collections.singletonList(currentColor);
        }

        public Game.Color getOwner() {
            return colors.get(colors.size() - 1);
        }

        public int getSize() {
            return colors.size();
        }

        public boolean getHasRed() {
            for (Game.Color color : colors) {
                if (color == Game.Color.RED) return true;
            }
            return false;
        }

        public List<Game.Color> getColors() {
            return colors;
        }
    }

    public class Row {
        private final int row;
        private final int startColumn;
        private final Position[] positions;

        private Row(int row, int startColumn, int endColumn) {
            this.row = row;
            this.startColumn = startColumn;
            this.positions = new Position[endColumn - startColumn];
            for (int i = 0; i < positions.length; i++) {
                this.positions[i] = new Position(this, i + startColumn);
            }
        }

        public int getNumber() {
            return row;
        }

        public Position[] getPositions() {
            return positions;
        }

        public Position getPosition(int column) {
            int targetColumn = column - startColumn;

            if (targetColumn >= 0 && targetColumn < positions.length) {
                return positions[targetColumn];
            } else {
                return null;
            }
        }
    }

}
