package com.nthalk.stacks.players;

import com.nthalk.stacks.Board;
import com.nthalk.stacks.Game;
import com.nthalk.stacks.Player;
import com.nthalk.stacks.exceptions.InvalidPlacementException;

import java.util.Random;
import java.util.Set;

public class RandomPlayer implements Player {
    Random random = new Random();

    private <T> T randomElement(Set<T> set) {
        int size = set.size();
        if (size == 0) return null;
        int pick = (size > 1) ? random.nextInt(size - 1) + 1 : 1;
        for (T t : set) {
            pick--;
            if (pick == 0) {
                return t;
            }
        }
        throw new IllegalStateException("This should not have happened");
    }

    @Override
    public Game.ValidMove move(Game.Color color, Game game) {
        Set<Board.Position> colorPositions = game.getColorPositions(color);
        while (colorPositions.size() > 0) {
            Board.Position from = randomElement(colorPositions);
            Set<Game.ValidMove> validMoves = game.getValidMoves(from);
            if (validMoves.size() != 0) {
                return randomElement(validMoves);
            }
            colorPositions.remove(from);
        }
        throw new IllegalStateException("This should not have happened");
    }

    @Override
    public Game.ValidPosition place(Game.Color color, Game game) {
        Set<Board.Position> emptyPositions = game.getEmptyPositions();
        try {
            return game.validate(randomElement(emptyPositions));
        } catch (InvalidPlacementException e) {
            throw new IllegalStateException("This should not have happened", e);
        }
    }
}
