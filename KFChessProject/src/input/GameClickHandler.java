package input;

import view.ClickListener;

public class GameClickHandler implements ClickListener {

        private final Controller controller;
        private long currentClock;

        public GameClickHandler(Controller controller) {
            this.controller = controller;
            this.currentClock = 0L;
        }

        public void setClock(long clock) {
            this.currentClock = clock;
        }

        @Override
        public void onClick(int x, int y) {
            controller.handleMouseClick(x, y, currentClock);
        }
}
