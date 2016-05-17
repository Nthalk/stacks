package com.nthalk.stacks;

import com.nthalk.fn.Option;
import com.nthalk.stacks.exceptions.InvalidMoveException;
import com.nthalk.stacks.exceptions.InvalidPlacementException;
import com.nthalk.stacks.players.RandomPlayer;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public class Game {

    private static final Logger LOG = Logger.getLogger(Game.class);

    private final Board board;
    private Phase phase;
    private Color currentColor;
    private Map<Color, Player> playersByColor = new IdentityHashMap<>();
    private Map<Board.Position, Board.Stack> stacksByPosition = new IdentityHashMap<>();

    public Game() {
        board = new Board();
        phase = Phase.PLACEMENT;
        currentColor = Color.WHITE;
    }

    public Board getBoard() {
        return board;
    }

    public ValidMove validate(Move move) throws InvalidMoveException {
        if (phase != Phase.PLAY) {
            throw new InvalidMoveException("Moves are only allowed in the play phase");
        }

        Board.Position from = move.getFrom();
        Option<Board.Stack> stackMoved = getStack(from);

        if (stackMoved.isEmpty()) {
            throw new InvalidMoveException("There is no stack to move");
        } else if (stackMoved.get().getOwner() != currentColor) {
            throw new InvalidMoveException("That stack is not yours to move");
        }

        Board.Position to = move.getTo();
        Option<Board.Stack> toStack = getStack(to);

        if (toStack.isEmpty()) {
            throw new InvalidMoveException("Cannot move to an empty space");
        }

        int stackSize = stackMoved.get().getSize();
        int fromRow = from.getRow().getNumber();
        int fromColumn = from.getColumn();
        int toRow = to.getRow().getNumber();
        int toColumn = to.getColumn();

        int rowDelta = Math.abs(fromRow - toRow);
        int columnDelta = Math.abs(fromColumn - toColumn);

        if ((rowDelta == 0 && columnDelta == stackSize) ||
            (columnDelta == 0 && rowDelta == stackSize) ||
            (columnDelta == rowDelta && columnDelta == stackSize)) {
            return new ValidMove(move);
        }

        throw new InvalidMoveException("Cannot move a stack of size " + stackSize + " from (" + fromColumn + "," + fromRow + ") to (" + fromColumn + "," + fromRow + ")");
    }

    public ValidPosition validate(Board.Position position) throws InvalidPlacementException {
        if (phase != Phase.PLACEMENT) {
            throw new InvalidPlacementException("Placements are only allowed in the placement phase");
        }

        if (!getStack(position).isEmpty()) {
            throw new InvalidPlacementException("Placement already has a piece on it");
        }

        return new ValidPosition(position);

    }

    public Set<ValidMove> getValidMoves(Board.Position from) {
        Set<ValidMove> validMoves = new HashSet<>();
        Option<Board.Stack> stackOption = getStack(from);
        if (stackOption.isEmpty()) {
            return validMoves;
        }

        Set<Board.Position> adjacentPositions = getBoard().adjacentPositions(from);
        if (adjacentPositions.size() == 6) {
            boolean holeFound = false;
            for (Board.Position position : adjacentPositions) {
                if (!getStack(position).isEmpty()) {
                    holeFound = true;
                }
            }
            if (!holeFound) {
                return validMoves;
            }
        }

        int stackSize = stackOption.get().getSize();


        Board.Row[] rows = board.getRows();
        int fromRow = from.getRow().getNumber();
        int fromColumn = from.getColumn();


        // Row, Column
        Board.Row row = from.getRow();
        Board.Position positionLeft = row.getPosition(fromColumn - stackSize);
        if (positionLeft != null && !getStack(positionLeft).isEmpty()) {
            // 0, -stack
            validMoves.add(new ValidMove(new Move(from, positionLeft)));
        }

        Board.Position positionRight = row.getPosition(fromColumn + stackSize);
        if (positionRight != null && !getStack(positionRight).isEmpty()) {
            // 0, +stack
            validMoves.add(new ValidMove(new Move(from, positionRight)));
        }

        if (fromRow > stackSize) {
            Board.Row rowDown = rows[fromRow - stackSize];
            Board.Position positionDown = rowDown.getPosition(fromColumn - stackSize);
            if (positionDown != null && !getStack(positionDown).isEmpty()) {
                // -stack, stack
                validMoves.add(new ValidMove(new Move(from, positionDown)));
            }
            positionDown = rowDown.getPosition(fromColumn + stackSize);
            if (positionDown != null && !getStack(positionDown).isEmpty()) {
                // -stack, -stack
                validMoves.add(new ValidMove(new Move(from, positionDown)));
            }
        }

        if (fromRow + stackSize < rows.length - 1) {
            Board.Row rowUp = rows[fromRow + stackSize];
            Board.Position positionUp = rowUp.getPosition(fromColumn - stackSize);
            if (positionUp != null && !getStack(positionUp).isEmpty()) {
                // stack, stack
                validMoves.add(new ValidMove(new Move(from, positionUp)));
            }
            positionUp = rowUp.getPosition(fromColumn + stackSize);
            if (positionUp != null && !getStack(positionUp).isEmpty()) {
                // stack, -stack
                validMoves.add(new ValidMove(new Move(from, positionUp)));
            }
        }

        for (ValidMove validMove : validMoves) {
            try {
                validate(validMove);
            } catch (InvalidMoveException e) {
                throw new RuntimeException(e);
            }
        }


        return validMoves;
    }

    public Option<Board.Stack> getStack(Board.Position position) {
        return Option.of(stacksByPosition.get(position));
    }

    public void setPlayer(Color white, RandomPlayer player) {
        playersByColor.put(white, player);
    }

    public boolean findValidPlay() {
        if (phase == Phase.PLACEMENT) {
            if (getEmptyPositions().size() == 0) {
                phase = Phase.PLAY;
            } else {
                return true;
            }
        }

        if (phase == Phase.PLAY) {
            for (Board.Position position : getColorPositions(currentColor)) {
                if (getValidMoves(position).size() > 0) {
                    return true;
                }
            }

            nextPlayer();

            for (Board.Position position : getColorPositions(currentColor)) {
                if (getValidMoves(position).size() > 0) {
                    return true;
                }
            }
        }

        if (phase != Phase.FINISHED) {
            LOG.info("Game finished: WHITE: " + getScore(Color.WHITE) + ", BLACK: " + getScore(Color.BLACK) + " WINNER: " + getWinner());
        }

        phase = Phase.FINISHED;

        return false;
    }

    public Set<Board.Position> getEmptyPositions() {
        Set<Board.Position> emptyPositions = new HashSet<>();
        for (Board.Position position : board.getPositions()) {
            if (getStack(position).isEmpty()) emptyPositions.add(position);
        }
        return emptyPositions;
    }

    public int getScore(Color color) {
        int score = 0;
        for (Board.Position position : getColorPositions(color)) {
            score += getStack(position).get().getSize();
        }
        return score;
    }

    public Set<Board.Position> getColorPositions(Color color) {
        Set<Board.Position> colorPositions = new HashSet<>();
        for (Board.Position position : board.getPositions()) {
            for (Board.Stack stack : getStack(position)) {
                if (stack.getOwner() == color) {
                    colorPositions.add(position);
                }
            }
        }
        return colorPositions;
    }

    public Color getWinner() {
        int whiteScore = getScore(Color.WHITE);
        int blackScore = getScore(Color.BLACK);
        if (whiteScore > blackScore) return Color.WHITE;
        if (blackScore > whiteScore) return Color.BLACK;
        return null;
    }

    private void nextPlayer() {
        if (currentColor == Color.BLACK) {
            currentColor = Color.WHITE;
        } else {
            currentColor = Color.BLACK;
        }
    }

    public Player getCurrentPlayer() {
        return playersByColor.get(currentColor);
    }

    public Phase getPhase() {
        return phase;
    }

    public Color getCurrentColor() {
        return currentColor;
    }

    public void submit(ValidPosition place) {

        if (getPositionsWithReds().size() <= 2) {
            LOG.info("Placement: " + currentColor.name() + " has placed RED at " + place.getPosition().toString());
            stacksByPosition.put(place.getPosition(), new Board.Stack(Color.RED));
        } else {
            LOG.info("Placement: " + currentColor.name() + " has placed at " + place.getPosition().toString());
            stacksByPosition.put(place.getPosition(), new Board.Stack(currentColor));
        }

        nextPlayer();
    }

    private Set<Board.Position> getPositionsWithReds() {
        Set<Board.Position> redPositions = new HashSet<>();
        for (Board.Position position : board.getPositions()) {
            for (Board.Stack stack : getStack(position)) {
                if (stack.getHasRed()) {
                    redPositions.add(position);
                }
            }
        }
        return redPositions;
    }

    private Set<Board.Position> connectedPositions(Board.Position position, Set<Board.Position> connectedPositions) {
        if (connectedPositions.contains(position)) {
            return connectedPositions;
        } else {
            connectedPositions.add(position);
            for (Board.Position adjancent : getBoard().adjacentPositions(position)) {
                if (!getStack(adjancent).isEmpty()) {
                    LOG.info("Position " + adjancent + " is connected to: " + position);
                    connectedPositions(adjancent, connectedPositions);
                }
            }
            return connectedPositions;
        }
    }

    public void submit(ValidMove move) {
        LOG.info("Move: " + currentColor.name() + " from " + move.getFrom().toString() + " to " + move.getTo().toString());
        Board.Stack upon = getStack(move.getTo()).get();
        Board.Stack place = getStack(move.getFrom()).get();
        stacksByPosition.remove(move.getFrom());
        stacksByPosition.put(move.getTo(), new Board.Stack(place, upon));

        Set<Board.Position> connectedPositions = new HashSet<>();

        for (Board.Position position : getPositionsWithReds()) {
            LOG.info("Red Position: " + position);
            connectedPositions(position, connectedPositions);
        }

        for (Board.Position position : getBoard().getPositions()) {
            if (!getStack(position).isEmpty() && !connectedPositions.contains(position)) {
                LOG.info("Clearing: " + position);
                stacksByPosition.remove(position);
            }
        }

        nextPlayer();
    }


    public enum Phase {
        PLACEMENT,
        PLAY,
        FINISHED
    }

    public enum Color {
        BLACK,
        WHITE,
        RED
    }

    public static class ValidPosition {
        private final Board.Position Position;

        public ValidPosition(Board.Position Position) {
            this.Position = Position;
        }

        public Board.Position getPosition() {
            return Position;
        }
    }

    public static class ValidMove extends Move {
        public ValidMove(Move move) {
            super(move.getFrom(), move.getTo());
        }
    }

    public static class Move {
        private final Board.Position from;
        private final Board.Position to;

        public Move(Board.Position from, Board.Position to) {
            this.from = from;
            this.to = to;
        }

        public Board.Position getFrom() {
            return from;
        }

        public Board.Position getTo() {
            return to;
        }
    }

}
