package input;

import view.ClickListener;
import view.FrameRenderer;

public class JumpClickHandler implements ClickListener {

    private final Controller controller;
    private final FrameRenderer frameRenderer;

    public JumpClickHandler(Controller controller, FrameRenderer frameRenderer) {
        this.controller = controller;
        this.frameRenderer = frameRenderer;
    }

    @Override
    public void onClick(int x, int y) {
        controller.handleJumpCommand(x, y);
        frameRenderer.renderNow();
    }
}