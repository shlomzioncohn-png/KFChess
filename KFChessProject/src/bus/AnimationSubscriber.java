package bus;
import bus.events.GameOverEvent;
import models.enums.PieceColor;

public class AnimationSubscriber implements EventListener {

    @Override
    public void onEvent(String topic, Object payload) {
        if (!(payload instanceof GameOverEvent event)) return;

        PieceColor winner = event.getWinner();
        System.out.println("[ANIMATION] game over animation, winner=" + winner);
    }
}