package tests;

import org.junit.jupiter.api.Test;
import view.FrameRenderer;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class GameClickHandlerTest {

    @Test
    public void frameRendererCanBeInvoked() {
        boolean[] wasCalled = {false};
        FrameRenderer fakeRenderer = () -> wasCalled[0] = true;   // <-- בלי פרמטר, תואם לממשק החדש

        fakeRenderer.renderNow();

        assertTrue(wasCalled[0]);
    }
}