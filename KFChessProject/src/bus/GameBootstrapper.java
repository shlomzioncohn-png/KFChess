package bus;

import bus.subscribers.LogSubscriber;
import bus.ScoreSubscriber;
import engine.GameEngine;
import models.Board;
import models.GameState;
import realtime.RealTimeArbiter;

public class GameBootstrapper {

    public static GameEngine buildEngine(Board board, RealTimeArbiter arbiter, GameState gameState) {
        EventBus bus = new EventBus();

        ScoreSubscriber scoreSubscriber = new ScoreSubscriber(gameState);
        LogSubscriber logSubscriber = new LogSubscriber(gameState);
        SoundSubscriber soundSubscriber = new SoundSubscriber();
        AnimationSubscriber animationSubscriber = new AnimationSubscriber();

        bus.subscribe("piece.captured", scoreSubscriber);
        bus.subscribe("piece.captured", soundSubscriber);
        bus.subscribe("move.completed", logSubscriber);
        bus.subscribe("move.completed", soundSubscriber);
        bus.subscribe("game.over", soundSubscriber);
        bus.subscribe("piece.promoted", soundSubscriber);
        bus.subscribe("move.illegal", soundSubscriber);
        return new GameEngine(board, arbiter, gameState, bus);
    }
}