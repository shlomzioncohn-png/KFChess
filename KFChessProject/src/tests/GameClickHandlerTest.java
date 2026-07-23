package tests;

import input.Controller;
import input.GameClickHandler;
import input.JumpClickHandler;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import view.FrameRenderer;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class GameClickHandlerTest {

    @Test
    public void onClickDelegatesToHandleMouseClickWithExactCoordinates() {
        Controller controller = mock(Controller.class);
        FrameRenderer renderer = mock(FrameRenderer.class);
        GameClickHandler handler = new GameClickHandler(controller, renderer);

        handler.onClick(37, 52);

        verify(controller, times(1)).handleMouseClick(37, 52);
        verify(controller, never()).handleJumpCommand(anyInt(), anyInt());
    }

    @Test
    public void onClickRendersAfterHandlingTheClickNotBefore() {
        Controller controller = mock(Controller.class);
        FrameRenderer renderer = mock(FrameRenderer.class);
        GameClickHandler handler = new GameClickHandler(controller, renderer);

        handler.onClick(10, 20);

        InOrder order = inOrder(controller, renderer);
        order.verify(controller).handleMouseClick(10, 20);
        order.verify(renderer).renderNow();
    }

    @Test
    public void jumpClickDelegatesToHandleJumpCommandWithExactCoordinatesNotToMouseClick() {
        Controller controller = mock(Controller.class);
        FrameRenderer renderer = mock(FrameRenderer.class);
        JumpClickHandler handler = new JumpClickHandler(controller, renderer);

        handler.onClick(64, 128);

        verify(controller, times(1)).handleJumpCommand(64, 128);
        verify(controller, never()).handleMouseClick(anyInt(), anyInt());
    }

    @Test
    public void jumpClickRendersAfterHandlingTheJumpNotBefore() {
        Controller controller = mock(Controller.class);
        FrameRenderer renderer = mock(FrameRenderer.class);
        JumpClickHandler handler = new JumpClickHandler(controller, renderer);

        handler.onClick(5, 6);

        InOrder order = inOrder(controller, renderer);
        order.verify(controller).handleJumpCommand(5, 6);
        order.verify(renderer).renderNow();
    }
}
