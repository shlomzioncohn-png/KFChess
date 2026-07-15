package input;

import view.ClickListener;
import view.FrameRenderer;

public class GameClickHandler implements ClickListener {

    private final Controller controller;
    private final FrameRenderer frameRenderer;
    private long currentClock;

    public GameClickHandler(Controller controller, FrameRenderer frameRenderer) {
        this.controller = controller;
        this.frameRenderer = frameRenderer;
        this.currentClock = 0L;
    }

    public void setClock(long clock) {
        this.currentClock = clock;
    }

    @Override
    public void onClick(int x, int y) {
        controller.handleMouseClick(x, y, currentClock);
        frameRenderer.renderNow(currentClock);
    }
}