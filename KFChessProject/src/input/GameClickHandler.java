package input;

import view.ClickListener;
import view.FrameRenderer;

public class GameClickHandler implements ClickListener {

    private final Controller controller;
    private final FrameRenderer frameRenderer;

    public GameClickHandler(Controller controller, FrameRenderer frameRenderer) {
        this.controller = controller;
        this.frameRenderer = frameRenderer;
    }

    @Override
    public void onClick(int x, int y) {
        controller.handleMouseClick(x, y);
        frameRenderer.renderNow();
    }
}