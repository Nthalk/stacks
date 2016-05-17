package com.nthalk.stacks;

import com.nthalk.stacks.players.RandomPlayer;
import org.junit.Test;

public class GameTest {

    @Test
    public void test() {
        Game game = new Game();
        game.setPlayer(Game.Color.WHITE, new RandomPlayer());
        game.setPlayer(Game.Color.BLACK, new RandomPlayer());
        while (game.findValidPlay()) {
            Player currentPlayer = game.getCurrentPlayer();
            if (game.getPhase() == Game.Phase.PLACEMENT) {
                game.submit(currentPlayer.place(game.getCurrentColor(), game));
            } else {
                game.submit(currentPlayer.move(game.getCurrentColor(), game));
            }
        }
    }
}