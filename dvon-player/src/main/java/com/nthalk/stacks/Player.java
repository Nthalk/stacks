package com.nthalk.stacks;

public interface Player {

    Game.ValidMove move(Game.Color color, Game game);

    Game.ValidPosition place(Game.Color color, Game game);

}
